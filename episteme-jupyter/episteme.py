import grpc
import sys
import os
import json
import time

# Protobuf generated code would normally be imported here
# For this demo, we mock the gRPC client structure

class EpistemeClient:
    """
    Python Client for Episteme Grid.
    Allows submitting tasks from Jupyter Notebooks.
    """
    
    def __init__(self, host='localhost', port=50051):
        self.channel = grpc.insecure_channel(f'{host}:{port}')
        self.token = None
        print(f"🔌 Connected to Episteme Grid at {host}:{port}")

    def login(self, username, password):
        """authenticate with the grid"""
        # Mocking the actual gRPC call for simplicity in this generated file
        # In real usage: stub.Login(...)
        if username == "admin" and password == "admin":
            self.token = "mock-jwt-token"
            print("✅ Login successful")
            return True
        print("❌ Login failed")
        return False

    def submit_task(self, task_type, params, priority='NORMAL'):
        """
        Submit a computational task to the grid.
        
        Args:
            task_type (str): 'mandelbrot', 'nbody', 'dna_folding', etc.
            params (dict): Task parameters
            priority (str): CRITICAL, HIGH, NORMAL, LOW
            
        Returns:
            str: Task ID
        """
        if not self.token:
            raise Exception("Not authenticated. Call login() first.")
            
        print(f"🚀 Submitting {task_type} task (Priority: {priority})...")
        # stub.SubmitTask(...)
        task_id = f"task-{int(time.time())}"
        print(f"📋 Task submitted: {task_id}")
        return task_id

    def get_status(self):
        """Get grid status"""
        return {
            "active_workers": 5,
            "queued_tasks": 12,
            "completed_tasks": 1450,
            "system_load": 0.45
        }

    def convert_units(self, value, from_unit, to_unit):
        """
        Convert scientific units using the Episteme engine.
        """
        print(f"🔄 Converting {value} {from_unit} to {to_unit}...")
        # In a real implementation, this would call a gRPC or MCP endpoint
        return value # Mock same value for now

    def get_constant(self, name, category=None):
        """
        Retrieve a scientific constant by name.
        """
        print(f"🔍 Fetching constant: {name}...")
        constants = {
            "PI": 3.141592653589793,
            "SPEED_OF_LIGHT": 299792458,
            "EARTH_MASS": 5.9722e24
        }
        return constants.get(name.upper(), "Unknown Constant")

    def get_data_model(self, name):
        """
        Retrieve a scientific data model (Spatial, Economic, etc.)
        """
        print(f"📦 Loading data model: {name}...")
        # Mocking generic response
        if "Spatial" in name:
            return {
                "model_type": "SPATIAL_GEOMETRY", 
                "metadata": {"name": "Global Migration", "source": "Synthetic"},
                "quantities": {
                    "locations_count": {"value": 150, "unit": "ONE"},
                    "total_magnitude": {"value": 1.25e7, "unit": "ONE"}
                }
            }
        elif "SIR" in name:
            return {
                "model_type": "EPIDEMIOLOGICAL_SIR",
                "metadata": {"name": "SIR Simulation", "virus": "Test Virus"},
                "quantities": {
                    "susceptible": {"value": 1000, "unit": "ONE"},
                    "infected": {"value": 10, "unit": "ONE"},
                    "r0": {"value": 2.5, "unit": "ONE"}
                }
            }
        return {"type": "GENERIC_MODEL", "name": name}



    def wait_for_result(self, task_id, timeout=30):
        """Poll for task result"""
        print(f"⏳ Waiting for result of {task_id}...")
        for i in range(timeout):
            time.sleep(1)
            # Check status...
            if i > 2: # Mock completion
                return {"status": "COMPLETED", "result": "Task result data object"}
        raise TimeoutError("Task timed out")

# Example Usage
if __name__ == "__main__":
    client = EpistemeClient()
    client.login("admin", "admin")
    tid = client.submit_task("dna_folding", {"sequence": "ACGT", "temp": 1.0})
    res = client.wait_for_result(tid)
    print(res)
