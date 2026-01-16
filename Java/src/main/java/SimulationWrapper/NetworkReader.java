package SimulationWrapper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import SimulationObjects.SimEdge;
import SimulationObjects.SimLane;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class NetworkReader {

    private final String netXMLfile;
    private final Set<SimEdge> edges = new HashSet<>();
    private final SumoWrapper wrapper;

    NetworkReader(String netXMLfile,SumoWrapper wrapper) {
        this.netXMLfile=netXMLfile;
        this.wrapper=wrapper;
    }


    public Set<SimEdge> readEdgeData() throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder db = factory.newDocumentBuilder();

        Document doc = db.parse(netXMLfile);

        NodeList edgeNodes = doc.getElementsByTagName("edge");
        System.out.println("Document length: "+edgeNodes.getLength());
        for (int i = 0; i < edgeNodes.getLength(); i++) {
            Element edgeElement = (Element) edgeNodes.item(i);

            String edgeId = edgeElement.getAttribute("id");


            SimEdge edge = new SimEdge(edgeId, wrapper);

            NodeList laneNodes = edgeElement.getElementsByTagName("lane");
            for (int j = 0; j < laneNodes.getLength(); j++) {
                Element laneEl = (Element) laneNodes.item(j);

                String laneId = laneEl.getAttribute("id");
                String shapeStr = laneEl.getAttribute("shape");
                List<Position> shape = parseShape(shapeStr);

                edge.addLane(new SimLane(laneId,wrapper, shape));
                }

                edges.add(edge);
            System.out.println("added: "+edgeId);
            }

            return edges;
        }

        private static List<Position> parseShape(String shape) {
            List<Position> result = new ArrayList<>();

            for (String token : shape.split(" ")) {
                String[] xy = token.split(",");
                result.add(new Position(
                        Double.parseDouble(xy[0]),
                        Double.parseDouble(xy[1])
                ));
            }
            return result;
    }
}

