import sys
import os
import site

# Print runtime diagnostics for easy debugging in Hugging Face logs
print("=== Episteme Agentic Playground Startup ===", flush=True)
print(f"Current UID: {os.getuid() if hasattr(os, 'getuid') else 'N/A'}", flush=True)
print(f"Current USER: {os.getenv('USER')}", flush=True)
print(f"Home Directory: {os.path.expanduser('~')}", flush=True)
print(f"Resolved User Site-Packages: {site.getusersitepackages()}", flush=True)

# Prioritize the resolved user site-packages directory in sys.path
sys.path.insert(0, site.getusersitepackages())
print(f"Configured sys.path: {sys.path}", flush=True)

# Deep runtime diagnostics
import importlib.metadata
import importlib.util
print("=== Deep Runtime Diagnostics ===", flush=True)
try:
    user_site_dir = site.getusersitepackages()
    if os.path.exists(user_site_dir):
        print(f"Files in user site-packages: {os.listdir(user_site_dir)[:20]} (total: {len(os.listdir(user_site_dir))})", flush=True)
    else:
        print("User site-packages directory does not exist!", flush=True)
except Exception as e:
    print(f"Failed to list user site-packages: {e}", flush=True)

try:
    print(f"Installed langchain version: {importlib.metadata.version('langchain')}", flush=True)
except Exception as e:
    print(f"Langchain version check failed: {e}", flush=True)

try:
    spec = importlib.util.find_spec('langchain')
    if spec:
        print(f"Langchain module spec origin: {spec.origin} (submodule search locations: {spec.submodule_search_locations})", flush=True)
    else:
        print("Langchain module spec NOT found!", flush=True)
except Exception as e:
    print(f"Langchain spec query failed: {e}", flush=True)
print("================================", flush=True)

import gradio as gr
import httpx
import json
import pandas as pd
from langchain_openai import ChatOpenAI
from langchain.agents import create_tool_calling_agent, create_react_agent
from langchain.agents import AgentExecutor
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder, PromptTemplate
from langchain_core.tools import tool
from dotenv import load_dotenv

load_dotenv()

# Configuration
MCP_SERVER_URL = os.getenv("MCP_SERVER_URL", "http://localhost:8080/mcp/message")
METRICS_URL = os.getenv("METRICS_URL", "http://localhost:8080/mcp/message") # Use JSON-RPC for metrics too

class EpistemeClient:
    def __init__(self, url):
        self.url = url

    def call(self, method, params=None):
        payload = {
            "jsonrpc": "2.0",
            "id": "agent-1",
            "method": method,
            "params": params or {}
        }
        try:
            response = httpx.post(self.url, json=payload, timeout=30.0)
            response.raise_for_status()
            return response.json().get("result", {})
        except Exception as e:
            return {"error": str(e)}

client = EpistemeClient(MCP_SERVER_URL)

# --- Tools ---
@tool
def convert_units(value: float, from_unit: str, to_unit: str):
    """Convert scientific units (e.g., from 'meters' to 'kilometers')."""
    return client.call("tools/call", {"name": "convert_units", "arguments": {"value": value, "from": from_unit, "to": to_unit}})

@tool
def get_constant(name: str):
    """Retrieve scientific constants (e.g., PI, G, SPEED_OF_LIGHT, EARTH_RADIUS)."""
    return client.call("tools/call", {"name": "get_constant", "arguments": {"name": name}})

@tool
def solve_expression(expression: str, guessMin: float, guessMax: float):
    """Find a root for f(x) = 0 using numerical methods (Brent). Useful for trajectories and impact points."""
    return client.call("tools/call", {"name": "solve_expression", "arguments": {"expression": expression, "guessMin": guessMin, "guessMax": guessMax}})

@tool
def execute_simulation(simulationType: str, parameters: dict):
    """Start a scientific simulation task (FLUID, NBODY, SIR, MIGRATION). Returns a taskId."""
    return client.call("tools/call", {"name": "execute_simulation", "arguments": {"simulationType": simulationType, "parameters": parameters}})

@tool
def get_task_status(taskId: str):
    """Check the status and result of a long-running task."""
    return client.call("tools/call", {"name": "get_task_status", "arguments": {"taskId": taskId}})

@tool
def calculate_matrix(matrixA: list, matrixB: list, op: str):
    """Perform high-performance matrix operations (ADD, SUBTRACT, MULTIPLY) on 2D arrays using bare-metal Java FFM."""
    return client.call("tools/call", {"name": "calculate_matrix", "arguments": {"matrixA": matrixA, "matrixB": matrixB, "op": op}})

@tool
def simplify_expression(expression: str):
    """Simplify a mathematical expression symbolically (e.g., 'x + x' -> '2x')."""
    return client.call("tools/call", {"name": "simplify_expression", "arguments": {"expression": expression}})

@tool
def read_hdf5_data(filePath: str, datasetPath: str):
    """Read a dataset from an HDF5 scientific file."""
    return client.call("tools/call", {"name": "read_hdf5_data", "arguments": {"filePath": filePath, "datasetPath": datasetPath}})

tools = [
    convert_units, get_constant, solve_expression, execute_simulation, 
    get_task_status, calculate_matrix, simplify_expression, read_hdf5_data
]

# --- Agent Prompt ---
prompt = ChatPromptTemplate.from_messages([
    ("system", "You are Episteme AI, an advanced bare-metal scientific assistant. Use the provided tools (including matrix, HDF5, expression simplification, and Brent solvers) to fulfill requests with maximum scientific accuracy."),
    MessagesPlaceholder(variable_name="chat_history"),
    ("user", "{input}"),
    MessagesPlaceholder(variable_name="agent_scratchpad"),
])

# --- ReAct Prompt for Free Llama 3.3 Model ---
react_template = """You are Episteme AI, an advanced bare-metal scientific assistant.
Use the provided tools (including matrix, HDF5, expression simplification, and Brent solvers) to fulfill requests with maximum scientific accuracy.

IMPORTANT: Do NOT write raw Python code blocks or use '<|python_tag|>'. If a scientific operation (matrix calculation, root-finding/solving, constant retrieval, unit conversion, simulation) is needed, you MUST call the appropriate tool from the list below.

You have access to the following tools:

{tools}

Use the following format:

Question: the input question you must answer
Thought: you should always think about what to do
Action: the action to take, should be one of [{tool_names}]
Action Input: the input to the action
Observation: the result of the action
... (this Thought/Action/Action Input/Observation can repeat N times)
Thought: I now know the final answer
Final Answer: the final answer to the original input question

Conversation History:
{chat_history}

Begin!

Question: {input}
Thought:{agent_scratchpad}"""

react_prompt = PromptTemplate.from_template(react_template)

# --- UI Functions ---
def get_metrics():
    res = client.call("tools/call", {"name": "get_server_metrics", "arguments": {}})
    content = res.get("content", {})
    if isinstance(content, str):
        try: content = json.loads(content)
        except: return "Error parsing metrics", "N/A", "N/A"
    
    latency = content.get("avg_latency", "0.00")
    memory = content.get("jvm_memory_usage", "0")
    ops = content.get("last_matrix_op_time", "0.00")
    return f"{latency:.2f} ms" if isinstance(latency, float) else f"{latency} ms", \
           f"{memory} MB", \
           f"{ops:.2f} ms" if isinstance(ops, float) else f"{ops} ms"

def chat_fn(message, history, user_api_key):
    user_api_key = user_api_key.strip() if user_api_key else ""
    
    try:
        if user_api_key.startswith("sk-"):
            # Paid Override: Custom OpenAI API Key -> Use GPT-4o-mini
            llm = ChatOpenAI(model="gpt-4o-mini", temperature=0, openai_api_key=user_api_key)
            provider_info = "⚡ *Running on OpenAI GPT-4o-mini (paid key override)*"
            agent = create_tool_calling_agent(llm, tools, prompt)
            agent_executor = AgentExecutor(agent=agent, tools=tools, verbose=False)
            is_react = False
        else:
            # Free Mode: Use Llama-3.3-70B on Hugging Face Serverless API
            hf_token = user_api_key if user_api_key.startswith("hf_") else os.getenv("HF_TOKEN", "")
            if not hf_token:
                return (
                    "⚠️ **Authentication Token Required**:\n\n"
                    "To run the free open-source model (Llama 3.3 70B), a **Hugging Face Access Token** is required.\n\n"
                    "👉 **How to get one (100% Free & Takes 10 Seconds)**:\n"
                    "1. Go to your Hugging Face Access Tokens page: https://huggingface.co/settings/tokens\n"
                    "2. Click **Create new token**, set the role to **Read** (or use your existing Write token), name it, and click **Create**.\n"
                    "3. Paste the generated token (starts with `hf_...`) in the **API Key Override** field below!\n\n"
                    "*(Tip: To log in permanently, add a Secret named `HF_TOKEN` in your Hugging Face Space settings!)*"
                )
            
            llm = ChatOpenAI(
                model="meta-llama/Llama-3.3-70B-Instruct:together",
                base_url="https://router.huggingface.co/v1",
                openai_api_key=hf_token,
                temperature=0
            )
            provider_info = "🍃 *Running on Free Hugging Face Serverless API (Llama 3.3 70B)*"
            agent = create_react_agent(llm, tools, react_prompt)
            agent_executor = AgentExecutor(agent=agent, tools=tools, verbose=False)
            is_react = True
        
        chat_history = []
        for h in history:
            # Support both Gradio 3/4 (list of lists) and Gradio 5/6 (gr.ChatMessage / dict)
            if hasattr(h, "role") and hasattr(h, "content"):
                role = h.role
                content = h.content
            elif isinstance(h, dict) and "role" in h and "content" in h:
                role = h["role"]
                content = h["content"]
            elif isinstance(h, (list, tuple)) and len(h) >= 2:
                chat_history.append(("user", h[0]))
                chat_history.append(("assistant", h[1]))
                continue
            else:
                continue
            
            if role == "user":
                chat_history.append(("user", content))
            elif role == "assistant":
                chat_history.append(("assistant", content))
            
        # Invoke agent
        if is_react:
            react_history_str = ""
            for role_name, content_text in chat_history:
                react_history_str += f"{role_name.capitalize()}: {content_text}\n"
            response = agent_executor.invoke({"input": message, "chat_history": react_history_str}, config={"callbacks": []})
        else:
            response = agent_executor.invoke({"input": message, "chat_history": chat_history}, config={"callbacks": []})
        return f"{provider_info}\n\n{response['output']}"
    except Exception as e:
        return f"❌ **Execution Error**: {str(e)}"

# --- Gradio UI & Custom CSS Styling ---
custom_css = """
/* Hide generic Gradio footer text and links, but preserve the theme toggle button */
footer a, footer span {
    display: none !important;
}
/* Ensure the container is perfectly responsive and fits within iframes */
.gradio-container {
    max-width: 1100px !important;
    margin: 0 auto !important;
    padding: 10px !important;
}
.premium-card {
    background: var(--background-fill-secondary);
    border: 1px solid var(--border-color-primary);
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 12px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
}
.premium-card h3 {
    margin-top: 0;
    color: var(--primary-600);
    font-weight: 700;
    font-size: 1.1rem;
    border-bottom: 1px solid var(--border-color-primary);
    padding-bottom: 6px;
    margin-bottom: 8px;
}
.feature-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 20px;
    font-size: 0.75rem;
    font-weight: 600;
    margin-right: 4px;
    margin-bottom: 4px;
    background: var(--primary-100);
    color: var(--primary-700);
    border: 1px solid var(--primary-200);
}
/* Ensure dark mode support for badge */
.dark .feature-badge {
    background: rgba(99, 102, 241, 0.2);
    color: var(--primary-300);
    border: 1px solid rgba(99, 102, 241, 0.4);
}
.comparison-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 10px;
    font-size: 0.85rem;
}
.comparison-table th, .comparison-table td {
    padding: 10px;
    text-align: left;
    border-bottom: 1px solid var(--border-color-primary);
}
.comparison-table th {
    background-color: var(--background-fill-primary);
    color: var(--primary-600);
    font-weight: 600;
}
"""

with gr.Blocks(title="Episteme Scientific Playground") as demo:
    # Shared state — holds the API key entered in the Preferences tab.
    # Using gr.State avoids rendering the Textbox twice (ChatInterface
    # would auto-render additional_inputs, causing DuplicateBlockError).
    api_key_state = gr.State(value="")
    # Compact Title Section for iframe compatibility
    gr.HTML("""
        <div style="text-align: center; padding: 10px 0; margin-bottom: 5px;">
            <div style="display: flex; align-items: center; justify-content: center; gap: 12px; margin-bottom: 5px;">
                <svg width="40" height="40" viewBox="0 0 100 100" style="background-color: #0b0f17; border-radius: 10px; border: 2px solid #3b82f6; box-shadow: 0 0 8px rgba(59, 130, 246, 0.5); display: inline-block;">
                    <text x="50" y="68" font-family="'Outfit', 'Inter', sans-serif" font-size="55" font-weight="900" fill="#3b82f6" text-anchor="middle">Σ</text>
                </svg>
                <h1 style="font-weight: 800; font-size: 1.8rem; margin: 0; color: var(--primary-600);">Episteme Scientific Playground</h1>
            </div>
            <p style="font-size: 0.95rem; opacity: 0.85; margin: 4px 0 0 0;">Bare-Metal Java 21 Kernel + Real-Time AI Agentic Orchestration</p>
        </div>
    """)
    
    # Collapsible Architecture & Token Info Grid
    with gr.Accordion("ℹ️ Learn more about Episteme Architecture & Jeton / Token setup (Click to Expand)", open=False):
        with gr.Row():
            with gr.Column(scale=2):
                gr.HTML("""
                    <div class="premium-card">
                        <h3>⚡ High-Performance Scientific Engine</h3>
                        <p style="font-size: 0.95rem; line-height: 1.6; margin: 0;">
                            Welcome to Episteme, a scientific computing playground. While traditional Python environments (like Jupyter or NumPy) suffer from interpreted latency, GIL lock bottlenecks, and heavy C-extension wrappers, Episteme is built on a <strong>compiled multi-threaded Java 21 back-end</strong>. By utilizing modern <strong>Foreign Function & Memory (FFM - Project Panama)</strong>, it executes complex math, symbolic simplifications, and spatial solvers natively on bare-metal hardware.
                        </p>
                        <div style="margin-top: 15px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
                            <span class="feature-badge">⚡ JVM FFM Panama</span>
                            <span class="feature-badge">📊 Bare-Metal Matrix Algebra</span>
                            <span class="feature-badge">🧬 DNA Sequence Solvers</span>
                            <span class="feature-badge">🪐 FPU Fluid Dynamics</span>
                            <span class="feature-badge">🤖 MCP Agent Bridge</span>
                            <a href="https://github.com/silveremartin/Episteme" target="_blank" style="display: inline-flex; align-items: center; gap: 6px; padding: 2px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; background: #24292e; color: #ffffff; border: 1px solid #2f363d; text-decoration: none; transition: all 0.2s;">
                                <svg height="14" width="14" viewBox="0 0 16 16" fill="currentColor" style="vertical-align: middle;">
                                    <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"></path>
                                </svg>
                                GitHub
                            </a>
                        </div>
                    </div>
                """)
            with gr.Column(scale=1):
                gr.HTML("""
                    <div class="premium-card">
                        <h3>🔗 Tokenless Free Mode</h3>
                        <p style="font-size: 0.88rem; line-height: 1.5; margin: 0 0 10px 0;">
                            This Space runs the state-of-the-art open-source model <strong>(Llama 3.3 70B)</strong>, hosted for free on Hugging Face Serverless.
                        </p>
                        <ol style="font-size: 0.82rem; padding-left: 15px; margin: 0; line-height: 1.4;">
                            <li>Generate a free token at <a href="https://huggingface.co/settings/tokens" target="_blank" style="color: var(--primary-600); text-decoration: underline;">HF Access Tokens</a>.</li>
                            <li>Paste your token (Read or Write) in the override field.</li>
                            <li>Ask complex questions below!</li>
                        </ol>
                    </div>
                """)
            
    with gr.Tabs():
        with gr.TabItem("🤖 Agentic Assistant"):
            gr.ChatInterface(
                chat_fn,
                additional_inputs=[api_key_state],
                examples=[
                    ["Perform a high-performance matrix multiplication between Matrix A [[1, 2], [3, 4]] and Matrix B [[5, 6], [7, 8]] using the bare-metal Java FFM FPU kernel."],
                    ["Find the root for the scientific equation cos(x) - x = 0 between 0 and 1 using the native numerical Brent solver."],
                    ["Run a multi-body physics simulation (simulationType: NBODY) with 5 bodies, a G-constant of 6.674e-11, and check the task results."],
                    ["Simplify the mathematical expression '(x^2 - 1) / (x - 1)' and convert the physical constant SPEED_OF_LIGHT to miles/hour."]
                ]
            )
            
        with gr.TabItem("🖥️ Kernel Monitor"):
            with gr.Row():
                m_lat = gr.Textbox(label="Avg Latency (P95)", value="0.00 ms", interactive=False)
                m_mem = gr.Textbox(label="JVM Off-heap Memory", value="0 MB", interactive=False)
                m_ops = gr.Textbox(label="Last Matrix Op", value="0.00 ms", interactive=False)
            
            btn_refresh = gr.Button("Refresh Metrics")
            btn_refresh.click(get_metrics, inputs=[], outputs=[m_lat, m_mem, m_ops])
            
            gr.HTML("""
                <div class="premium-card" style="margin-top: 25px;">
                    <h3>📊 Runtime Comparison: Episteme vs. Python (NumPy/Jupyter)</h3>
                    <table class="comparison-table">
                        <thead>
                            <tr>
                                <th>Feature</th>
                                <th>Episteme (Bare-Metal Java)</th>
                                <th>Traditional Python (NumPy / GIL)</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><strong>Memory Management</strong></td>
                                <td style="color: var(--primary-600); font-weight: 600;">High-performance off-heap JVM Memory (No GC Overhead)</td>
                                <td>On-heap Python objects with heavy reference counting & garbage collector latency</td>
                            </tr>
                            <tr>
                                <td><strong>Hardware Mapping</strong></td>
                                <td style="color: var(--primary-600); font-weight: 600;">Direct FFM (Panama) low-latency native hardware mapping</td>
                                <td>Heavy runtime C-extension wrappers (pybind11 wrap overhead)</td>
                            </tr>
                            <tr>
                                <td><strong>Multithreading & SIMD</strong></td>
                                <td style="color: var(--primary-600); font-weight: 600;">Native SIMD vector instruction pipelines & true multi-core grid scalability</td>
                                <td>GIL-bound interpreter (Global Interpreter Lock bottlenecks multi-threaded performance)</td>
                            </tr>
                            <tr>
                                <td><strong>Bridge Architecture</strong></td>
                                <td style="color: var(--primary-600); font-weight: 600;">SSE JSON-RPC / gRPC Microservice Mesh</td>
                                <td>Standard Jupyter Monolithic loop</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            """)
            
            gr.Markdown("### 🔌 Claude Desktop Configuration")
            gr.Code(value="""{
  "mcpServers": {
    "episteme": {
      "command": "npx",
      "args": ["-y", "@episteme-hcp/mcp-bridge", "--url", "https://episteme-hcp-episteme.hf.space/mcp/sse"]
    }
  }
}""", language="json")
            
        with gr.TabItem("⚙️ Preferences"):
            gr.HTML("""
                <div class="premium-card">
                    <h3>🔑 API Key &amp; Authentication Setup</h3>
                    <p style="font-size: 0.9rem; line-height: 1.5; margin: 0 0 12px 0;">
                        By default, this Space runs the state-of-the-art open-source model <strong>Llama 3.3 70B</strong> for free using the Hugging Face Serverless API.
                    </p>
                    <p style="font-size: 0.9rem; line-height: 1.5; margin: 0 0 15px 0;">
                        If you want to use a paid OpenAI model (like <strong>GPT-4o-mini</strong>) or use your own Hugging Face token, you can configure it below. Your credentials are only used for your current session and are never saved on the server.
                    </p>
                </div>
            """)
            # Rendered here ONCE — synced into api_key_state on every keystroke
            user_api_key_input = gr.Textbox(
                type="password",
                label="API Key Override (Optional)",
                placeholder="Optional: Paste an OpenAI (sk-...) or Hugging Face (hf_...) token to override default settings",
                value=""
            )
            user_api_key_input.change(
                fn=lambda key: key,
                inputs=[user_api_key_input],
                outputs=[api_key_state]
            )

if __name__ == "__main__":
    demo.launch(
        server_name="0.0.0.0", 
        server_port=7860, 
        theme=gr.themes.Soft(primary_hue="indigo", secondary_hue="slate"),
        css=custom_css
    )
