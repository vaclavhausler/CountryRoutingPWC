package com.vhausler.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhausler.config.CountriesProperties;
import com.vhausler.model.Country;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final CountriesProperties properties;
    private final Map<String, List<String>> graph = new HashMap<>();

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        List<Country> countries;

        try (InputStream is = URI.create(properties.getUrl())
                .toURL()
                .openStream()) {
            countries = mapper.readValue(is, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.warn("Failed to load countries from remote URL, falling back to embedded resource. Remote URL: {}", properties.getUrl(), e);
            // Fallback to embedded resource
            try (InputStream is = getClass().getResourceAsStream("/json/countries.json")) {
                if (is == null) {
                    throw new IllegalStateException("No country data available", e);
                }
                countries = mapper.readValue(is, new TypeReference<>() {
                });
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to load local country data", ex);
            }
        }

        // Build graph
        log.debug("Loaded {} countries from data source", countries.size());
        for (Country c : countries) {
            graph.put(c.getCca3(),
                    c.getBorders() == null ? List.of() : c.getBorders());
        }
    }

    public List<String> findRoute(String origin, String destination) {
        if (!graph.containsKey(origin) || !graph.containsKey(destination)) {
            return null; // NOSONAR, intentional
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(destination)) {
                return buildPath(parent, origin, destination);
            }

            for (String neighbor : graph.getOrDefault(current, List.of())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return null; // NOSONAR, intentional
    }

    private List<String> buildPath(Map<String, String> parent,
                                   String origin,
                                   String destination) {
        LinkedList<String> path = new LinkedList<>();
        String step = destination;

        while (step != null) {
            path.addFirst(step);
            step = parent.get(step);
        }

        return path.getFirst().equals(origin) ? path : null;
    }
}
