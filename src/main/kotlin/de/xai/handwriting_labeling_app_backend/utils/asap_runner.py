import sys
import json
import numpy as np
import asap_cpu

def main():
    input_data = sys.stdin.read()
    data = json.loads(input_data)

    pwc_mat = np.array(data["matrix"])
    N = pwc_mat.shape[0]

    asap = asap_cpu.ASAP(N, selective_eig=True)
    kl_divs, pairs_to_compare = asap.run_asap(pwc_mat, mst_mode=True)

    # Compute max EIG (ignoring -1s)
    valid_eigs = kl_divs[kl_divs >= 0]
    max_eig = float(valid_eigs.max()) if valid_eigs.size > 0 else 0.0

    output = {
        "pairs": [list(pair) for pair in pairs_to_compare],
        "max_eig": max_eig
    }

    print(json.dumps(output))
    sys.stdout.flush()

if __name__ == "__main__":
    main()
