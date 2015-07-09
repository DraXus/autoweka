import os
import argparse
from os import system
import sqlite3
from config import *

def main():
    parser = argparse.ArgumentParser(prog=os.path.basename(__file__))
    parser.add_argument('--dataset', choices=datasets, required=False)
    parser.add_argument('--strategy', choices=strategies, required=False)
    parser.add_argument('--generation', choices=generations, required=False)
    parser.add_argument('--seed', choices=seeds, required=False)

    args = parser.parse_args()

    # override default values
    if args.dataset:
        selected_datasets = [args.dataset]
    else:
        selected_datasets = datasets
    if args.strategy:
        selected_strategies = [args.strategy]
    else:
        selected_strategies = strategies
    if args.generation:
        selected_generations = [args.generation]
    else:
        selected_generations = generations
    if args.seed:
        selected_seeds = [args.seed]
    else:
        selected_seeds = seeds

    for dataset in selected_datasets:
        for strategy in selected_strategies:
            if strategy not in ['DEFAULT', 'RAND']:
                for generation in selected_generations:
                    for seed in selected_seeds:
                        d = {"dataset": dataset, "strategy": strategy, "generation": generation}
                        experiment = "{dataset}.{strategy}.{generation}-{dataset}".format(**d)

                        command = 'qsub -N %s -l q=compute ./launch_training_prediction.sh %s %s %s' % (
                            experiment, experiment, dataset, seed)
                        print command
                        system(command)


if __name__ == "__main__":
    main()
