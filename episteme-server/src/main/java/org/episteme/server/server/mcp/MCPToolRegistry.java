/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.episteme.server.server.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for executable MCP Tools.
 * Scans for services annotated with @MCPTool and registers them.
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
@Service
public class MCPToolRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(MCPToolRegistry.class);
    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();

    public MCPToolRegistry() {
    }

    @PostConstruct
    public void init() {
        // Core diagnostic tools
        registerTool("convert_units", "Convert scientific units", 
            "{\"type\": \"object\", \"properties\": {\"value\": {\"type\": \"number\"}, \"from\": {\"type\": \"string\"}, \"to\": {\"type\": \"string\"}}, \"required\": [\"value\", \"from\", \"to\"]}");
        registerTool("get_constant", "Retrieve scientific constants (e.g., PI, SPEED_OF_LIGHT, EARTH_MASS)",
            "{\"type\": \"object\", \"properties\": {\"category\": {\"type\": \"string\", \"enum\": [\"MATH\", \"PHYSICS\", \"EARTH\", \"GEOGRAPHY\", \"HISTORY\"]}, \"name\": {\"type\": \"string\"}}, \"required\": [\"name\"]}");
        registerTool("calculate_matrix", "Perform matrix operations (add, subtract, multiply)", 
            "{\"type\": \"object\", \"properties\": {\"matrixA\": {\"type\": \"array\", \"items\": {\"type\": \"array\", \"items\": {\"type\": \"number\"}}}, \"matrixB\": {\"type\": \"array\", \"items\": {\"type\": \"array\", \"items\": {\"type\": \"number\"}}}, \"op\": {\"type\": \"string\", \"enum\": [\"ADD\", \"SUBTRACT\", \"MULTIPLY\"]}}, \"required\": [\"matrixA\", \"matrixB\", \"op\"]}");
        registerTool("simplify_expression", "Simplify a mathematical expression (e.g., 'x + x' -> '2x')",
            "{\"type\": \"object\", \"properties\": {\"expression\": {\"type\": \"string\"}}, \"required\": [\"expression\"]}");
        registerTool("solve_expression", "Find a root for f(x) = 0 using numerical methods (Brent)",
            "{\"type\": \"object\", \"properties\": {\"expression\": {\"type\": \"string\"}, \"guessMin\": {\"type\": \"number\"}, \"guessMax\": {\"type\": \"number\"}}, \"required\": [\"expression\", \"guessMin\", \"guessMax\"]}");
        registerTool("read_hdf5_data", "Read a dataset from an HDF5 scientific file",
            "{\"type\": \"object\", \"properties\": {\"filePath\": {\"type\": \"string\"}, \"datasetPath\": {\"type\": \"string\"}}, \"required\": [\"filePath\", \"datasetPath\"]}");
        registerTool("get_server_metrics", "Retrieve real-time server and grid performance metrics",
            "{\"type\": \"object\", \"properties\": {}}");
        registerTool("execute_simulation", "Start a scientific simulation task",
            "{\"type\": \"object\", \"properties\": {\"simulationType\": {\"type\": \"string\", \"enum\": [\"FLUID\", \"NBODY\", \"SIR\", \"MIGRATION\"]}, \"parameters\": {\"type\": \"object\"}}, \"required\": [\"simulationType\"]}");

        registerTool("calculate_series", "Compute Taylor or Maclaurin series expansion of functions (exp, sin, cos) using Episteme's native symbolic power series engine",
            "{\"type\": \"object\", \"properties\": {\"function\": {\"type\": \"string\", \"enum\": [\"exp\", \"sin\", \"cos\"]}, \"order\": {\"type\": \"integer\"}}, \"required\": [\"function\", \"order\"]}");

        registerTool("get_task_status", "Check the status and result of a long-running task",
            "{\"type\": \"object\", \"properties\": {\"taskId\": {\"type\": \"string\"}}, \"required\": [\"taskId\"]}");
        
        /* 
        // Dynamic discovery of AlgorithmProviders is disabled for security on production (Hugging Face)
        // We favor explicit registration to ensure a rigid and audited contract.
        var providers = context.getBeansOfType(org.episteme.core.technical.algorithm.AlgorithmProvider.class);
        for (var provider : providers.values()) {
            if (provider.isAvailable()) {
                String toolName = provider.getName().toLowerCase().replace(" ", "_").replace("/", "_");
                String description = String.format("Execute scientific algorithm: %s (%s)", provider.getName(), provider.getAlgorithmType());
                String schema = "{\"type\": \"object\", \"properties\": {\"params\": {\"type\": \"object\"}}}";
                registerTool(toolName, description, schema);
            }
        }
        */

        LOG.info("Registered {} dynamic MCP tools from grid algorithms", tools.size());
    }

    public void registerTool(String name, String description, String jsonSchema) {
        tools.put(name, new ToolDefinition(name, description, jsonSchema));
    }

    public ToolDefinition getTool(String name) {
        return tools.get(name);
    }
    
    public Map<String, ToolDefinition> getAllTools() {
        return tools;
    }

    public record ToolDefinition(String name, String description, String inputSchema) {}
}
