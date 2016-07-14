#!/bin/bash

cur="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# the script is split into two parts, definitions and execution,
# which allows for "overwriting" of functions in the tests
# (i.e. avoid call to actuall binary like cerbot and trigger external request)
source "${cur}/_def_"
source "${cur}/_exec_"
