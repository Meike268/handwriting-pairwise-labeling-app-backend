import sys
import json
import numpy as np
import asap_cpu



def main():
    try:
        # read input data from kotlin
        input_data = sys.stdin.read()
        data = json.loads(input_data)

        # prepare the matrix and get number of samples (N)
        pwc_mat = np.array(data["matrix"])
        N = pwc_mat.shape[0]

        # run the ASAP algorithm
        asap = asap_cpu.ASAP(N, selective_eig=True, approx=False)
        inf_mat, pairs_to_compare = asap.run_asap(pwc_mat, mst_mode=True)

        # Compute max EIG (ignoring -1s)
        # valid_eigs = inf_mat[inf_mat >= 0]
        # max_eig = float(valid_eigs.max()) if valid_eigs.size > 0 else 0.0

        # Compute mean EIG (ignoring -1s)
        valid_eigs = inf_mat[inf_mat >= 0]
        mean_eig = float(valid_eigs.mean()) if valid_eigs.size > 0 else 0.0

        # output a list of index pairs and the max_eig
        output = {
            "pairs": [list(pair) for pair in pairs_to_compare],
            "mean_eig": mean_eig
        }

        # serialize the dictionary to a JSON string
        print(json.dumps(convert(output)))

        # flush output to standard output
        sys.stdout.flush()

    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.stdout.flush()
        sys.exit(1)

def convert(obj):
    if isinstance(obj, np.integer):
        return int(obj)
    elif isinstance(obj, np.floating):
        return float(obj)
    elif isinstance(obj, np.ndarray):
        return obj.tolist()
    elif isinstance(obj, (list, tuple)):
        return [convert(i) for i in obj]
    elif isinstance(obj, dict):
        return {k: convert(v) for k, v in obj.items()}
    else:
        return obj

if __name__ == "__main__":
    main()
