# Strategy Manager for Nemoulous

IGNORE THIS FOR NOW. It is out of date!

## Prerequisites

1. You are running on a Mac or *nix machine that has access to a bash-like shell
2. Docker must be installed on your computer

## Setup

Run the setup.sh file from the command line:

`sh setup.sh`

This command will set up a docker network named nemoulous and add containers running the following to it:
1. zookeeper
2. kafka
3. postgresql 

It will then create a container to compile the scala code here, and then emit the compiled artifact to be run in a different, clean container.




