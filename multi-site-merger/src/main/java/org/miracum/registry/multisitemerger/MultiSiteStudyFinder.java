package org.miracum.registry.multisitemerger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MultiSiteStudyFinder {

  private static final Logger log = LoggerFactory.getLogger(MultiSiteStudyFinder.class);

  /**
   * Given a flat list of ResearchStudy resources, this method returns a list of lists where each
   * list represents a cluster of ResearchStudy resources describing the same master study. Affinity
   * to a master study is determined by the ResearchStudy's identifiers.
   *
   * @param studies the list of ResearchStudy resource from which to construct the master study
   *     sets.
   * @return a list of lists where each list represents a set of ResearchStudy resources belonging
   *     to the same master study.
   */
  public List<Set<ResearchStudy>> findMultiSiteStudies(List<ResearchStudy> studies) {
    if (studies == null) {
      throw new IllegalArgumentException("studies may not be null");
    }

    if (studies.isEmpty()) {
      return List.of();
    }

    if (studies.size() == 1) {
      return List.of(Set.of(studies.get(0)));
    }

    var graph = new DefaultUndirectedGraph<ResearchStudy, DefaultEdge>(DefaultEdge.class);

    /*
     * mapping between an identifier and a list of ResearchStudys with the same identifier
     * this is necessary to construct the graph later on as we need to know beforehand what
     * nodes are connected via the same edge. There may be a cleverer solution for this, though.
     */
    var edgeMap = new HashMap<String, ArrayList<ResearchStudy>>();

    for (var study : studies) {
      graph.addVertex(study);

      for (var identifier : study.getIdentifier()) {
        if (!identifier.hasValue() || !identifier.hasSystem()) {
          log.warn(
              "{} has identifier {} without value. Skipping.",
              study.getId(),
              identifier.getSystem());
          continue;
        }
        var identifierString =
            String.format(
                "%s|%s",
                identifier.getSystem().toLowerCase().trim(),
                identifier.getValue().toLowerCase().trim());
        edgeMap.putIfAbsent(identifierString, new ArrayList<>());
        edgeMap.get(identifierString).add(study);
      }
    }

    for (var entry : edgeMap.entrySet()) {
      var vertices = entry.getValue();

      if (vertices.size() == 1) {
        continue;
      }

      for (int i = 0; i < vertices.size() - 1; i++) {
        var source = vertices.get(i);
        var target = vertices.get(i + 1);

        if (!graph.containsEdge(source, target)) {
          graph.addEdge(source, target);
        }
      }
    }

    var connectivity = new ConnectivityInspector<>(graph);

    return connectivity.connectedSets();
  }
}
