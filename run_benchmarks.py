import os
import subprocess
import json
import shutil

backends = {
    "lumberjack": "LumberjackBenchmark",
    "log4j": "Log4jBenchmark",
    "logback": "LogbackBenchmark",
    "jul": "JulBenchmark"
}

def run_command(command):
    print(f"Running: {command}")
    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Command failed with return code {result.returncode}")
        print(f"STDOUT: {result.stdout}")
        print(f"STDERR: {result.stderr}")
    return result.returncode == 0

def collect_results():
    all_results = []
    results_dir = "benchmark_results_json"
    if os.path.exists(results_dir):
        shutil.rmtree(results_dir)
    os.makedirs(results_dir)

    for backend, benchmark_class in backends.items():
        print(f"Testing backend: {backend}")
        cmd = f"./gradlew :lumberjack:benchmark:jmh -Pbackend={backend} -Pjmh.includes='{benchmark_class}'"
        if run_command(cmd):
            src_json = "lumberjack/benchmark/build/results/jmh/results.json"
            dest_json = os.path.join(results_dir, f"results_{backend}.json")
            if os.path.exists(src_json):
                shutil.copy(src_json, dest_json)
                with open(dest_json, 'r') as f:
                    data = json.load(f)
                    for entry in data:
                        entry['backend'] = backend
                    all_results.extend(data)
            else:
                print(f"Warning: Result file {src_json} not found for {backend}")
        else:
            print(f"Error: Benchmark failed for {backend}")

    return all_results

def generate_markdown(results):
    if not results:
        print("No results to process.")
        return

    # Group by category and format
    grouped = {}
    for entry in results:
        params = entry.get('params', {})
        category = params.get('category', 'N/A')
        fmt = params.get('format', 'N/A')
        key = (category, fmt)
        if key not in grouped:
            grouped[key] = []
        grouped[key].append(entry)

    with open("BENCHMARK_RESULTS.md", "w") as f:
        f.write("# Logging Benchmark Results\n\n")
        
        # Sort keys: CONSOLE first, then FILE; then by format
        sorted_keys = sorted(grouped.keys())

        for key in sorted_keys:
            category, fmt = key
            f.write(f"## Category: {category}, Format: {fmt}\n\n")
            f.write("| Backend | Benchmark | Score (ops/s) | Error |\n")
            f.write("|---------|-----------|---------------|-------|\n")
            
            entries = grouped[key]
            # Sort by score descending
            entries.sort(key=lambda x: x['primaryMetric']['score'], reverse=True)
            
            for entry in entries:
                backend = entry['backend']
                benchmark = entry['benchmark'].split('.')[-1]
                score = entry['primaryMetric']['score']
                error = entry['primaryMetric']['scoreError']
                f.write(f"| {backend} | {benchmark} | {score:.2f} | {error:.2f} |\n")
            f.write("\n")

    print("Markdown report generated: BENCHMARK_RESULTS.md")

if __name__ == "__main__":
    results = collect_results()
    generate_markdown(results)
