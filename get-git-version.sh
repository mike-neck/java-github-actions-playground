#!/usr/bin/env bash

git log --oneline -n 1 | awk '{print $1}'
