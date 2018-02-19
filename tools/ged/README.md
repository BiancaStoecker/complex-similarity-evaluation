# Graph Edit Distance
Implements the approach described in:

    Lerouge, J.; Abu-Aisheh, Z.; Raveaux, R.; Héroux, P. & Adam, S. 
    New binary linear programming formulation to compute the graph edit distance 
    Pattern Recognition, 2017, 72, 254 - 265

## Requirements
Gurobi is required for solving the binary linear program. The libraries `libgurobi75.so`, `libGurobiJni75.so` and `gurobi.jar` must be installed system-wide or copied to the folder `lib`. A Gurobi licence key must be installed.

## Usage
The following command computes the edit distance between the query graph specified in `ged_query.gml` and all database graphs contained in `ged_db.gml`:
```
./ged ged_query.gml ged_db.gml
```
The output is a list of all database graphs sorted by their distance to the query graph. For each database graph the output conistst of one line with the following tab-separated values:
* Number of vertices in the query graph
* Number of edges in the query graph
* Number of vertices in the database graph
* Number of edges in the database graph
* Graph edit cost

## Links
A free academic licence of Gurobi can be obtained [here](http://www.gurobi.com/academia/for-universities).