package org.neo4j.talend;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.neo4j.tooling.ImportTool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jImportTool {

    /**
     * The logger
     */
    private final static Logger log = Logger.getLogger(Neo4jImportTool.class);

    protected final static String HEADERS_KEY = "HEADERS";
    protected final static String FILE_KEY = "FILE";

    private String dbPath;
    private Map<String, String> neo4jConfiguration;
    private Map<String, String> importConfiguration;
    private List<Map<String, String>> nodes;
    private List<Map<String, String>> relationships;

    /**
     * Constructor.
     *
     * @param dbPath
     * @param neo4jConfiguration
     * @param importConfiguration
     * @param nodes
     * @param relationships
     */
    public Neo4jImportTool(String dbPath, Map<String, String> neo4jConfiguration, Map<String, String> importConfiguration, List<Map<String, String>> nodes, List<Map<String, String>> relationships) {
        this.dbPath = dbPath;
        this.neo4jConfiguration = neo4jConfiguration;
        this.importConfiguration = importConfiguration;
        this.nodes = nodes;
        this.relationships = relationships;
    }

    /**
     * Execute the import.
     *
     * @throws IOException
     */
    public void execute() throws IOException {
        List<String> params = new ArrayList<>();

        // Adding dbPath
        params.add("--into");
        params.add(dbPath);

        // Adding neo4j config file
        params.add("--db-config");
        params.add(this.createNeo4jConfigFile());

        // Adding advanced configuration
        for (Map.Entry<String, String> entry : importConfiguration.entrySet()) {
            params.add("--" + entry.getKey());
            params.add(entry.getValue());
        }

        // Adding node files
        for (Map<String, String> node : nodes) {
            params.add("--nodes");
            params.add(this.createHeaderFile(node.get(HEADERS_KEY)) + "," + node.get(FILE_KEY));
        }

        // Adding relationships files
        for (Map<String, String> relationship : relationships) {
            params.add("--relationships");
            params.add(this.createHeaderFile(relationship.get(HEADERS_KEY)) + "," + relationship.get(FILE_KEY));
        }

        log.trace("Launch import tool with args : " + StringUtils.join(params, " "));

        // execute the import tool
        ImportTool.main(params.toArray(new String[params.size()]));
    }

    /**
     * Create the specified header file.
     *
     * @param header Content of the file
     * @return Full path of the generated file
     * @throws IOException
     */
    protected String createHeaderFile(String header) throws IOException {
        // create file
        Path path = Files.createTempFile("importToolHeader", ".csv");

        // write header into it
        BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));
        bw.write(header);
        bw.close();


        return path.toFile().getAbsolutePath();
    }

    /**
     * Create Neo4j configuration file.
     *
     * @return Full path of the generated file
     * @throws IOException
     */
    protected String createNeo4jConfigFile() throws IOException {
        // create file
        Path path = Files.createTempFile("neo4jConfig", ".properties");

        // write header into it
        BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));
        for (Map.Entry<String, String> entry : neo4jConfiguration.entrySet()) {
            bw.write(entry.getKey() + "=" + entry.getValue());
            bw.newLine();
        }
        bw.close();

        return path.toFile().getAbsolutePath();
    }
}
