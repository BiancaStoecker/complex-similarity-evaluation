# Graph Edit Similarity Range Queries
Implements GED similarity based range queries using lower bounds for filtering and upper bounds for verification.


## Requirements
Gurobi is required for solving the binary linear program. The libraries `libgurobi75.so`, `libGurobiJni75.so` and `gurobi.jar` must be installed system-wide or copied to the folder `lib`. A Gurobi licence key must be installed.

## Usage
The following command finds for each graph in `ged_query.gml` all the database graphs from `ged_db.gml` that have graph edit simiarity of at least `0.9`:
```
./ged_rangequery ged_db.gml ged_query.gml 0.9 out.txt
```
The total time required for filtering and verification is written to the file `out.txt`.

## Links
A free academic licence of Gurobi can be obtained [here](http://www.gurobi.com/academia/for-universities).
