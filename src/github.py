import sys
from subprocess import Popen, PIPE
import matplotlib.pyplot as plt
import numpy as np
import plotly.plotly as py


def filter_proj_by_stars(file):
    file = open(file, 'r')
    stars_map = {}
    projs = []
    sum = 0
    stars = 0

    for line in file:
        if "Stars:" in line:
            stars = int(line.split("Stars:")[1].strip())
            if stars > 0:
                sum += 1
                # print(stars, end=',')
                # if stars in stars_map:
                #     stars_map[stars] += 1
                # else:
                #     stars_map[stars] = 1
        if "Clone url: " in line and stars > 0:
            repo = line.split("Clone url: ")[1].strip().replace("https://github.com/", "").replace(".git", "")
            # print(repo)
            projs.append([stars, repo])

    # print(stars_map)
    # print(sum)
    return projs


def gen_config_template():
    config = list()
    config.append(["elasticsearchLoc", "/Users/Chaiyong/IdeasProjects/Siamese/elasticsearch-2.2.0"])
    config.append(["server", "localhost"])
    config.append(["index", "github"])
    config.append(["type", "siamese"])
    config.append(["inputFolder", "/Users/Chaiyong/Downloads/github"])
    config.append(["subInputFolder", ""])
    config.append(["outputFolder", "/Users/Chaiyong/Downloads/isics_results"])
    config.append(["normMode", "djkspvw"])
    config.append(["isNgram", "true"])
    config.append(["ngramSize", "4"])
    config.append(["dfs", "true"])
    config.append(["writeToFile", "true"])
    config.append(["extension", "java"])
    config.append(["minCloneSize", "10"])
    config.append(["command", "index"])
    config.append(["recreateIndexIfExists", "false"])
    config.append(["isPrint", "false"])
    config.append(["outputFormat", "csvfline"])
    config.append(["indexingMode", "bulk"])
    config.append(["bulkSize", "4000"])
    config.append(["methodParser", "crest.siamese.helpers.JavaMethodParser"])
    config.append(["tokenizer", "crest.siamese.helpers.JavaTokenizer"])
    config.append(["normalizer", "crest.siamese.helpers.JavaNormalizer"])
    config.append(["multirep", "true"])
    config.append(["resultOffset", "0"])
    config.append(["resultsSize", "10"])
    config.append(["totalDocuments", "100"])
    config.append(["parseMode", "method"])
    config.append(["printEvery", "10000"])
    config.append(["rankingFunction", "tfidf"])
    config.append(["queryReduction", "true"])
    config.append(["QRPercentileNorm", "25"])
    config.append(["QRPercentileOrig", "75"])
    config.append(["normBoost", "30"])
    config.append(["origBoost", "1"])
    config.append(["similarityMode", "tfidf_text_def"])
    config.append(["cloneClusterFile", "soco"])
    config.append(["errorMeasure", "map"])
    config.append(["deleteIndexAfterUse", "true"])
    config.append(["license", "true"])
    config.append(["licenseExtractor", "regexp"])
    config.append(["github", "true"])
    return config


def update_config(config, index, val):
    config[index][1] = val
    return config


def write_config(config):
    config_str = ""
    for line in config:
        config_str += line[0] + "=" + line[1] + "\n"
    writefile("myconfig.properties", config_str, 'w', False)


def writefile(filename, fcontent, mode, isprint):
    """
    Write the string content to a file
    copied from
    http://www.pythonforbeginners.com/files/reading-and-writing-files-in-python
    :param filename: name of the file
    :param fcontent: string content to put into the file
    :param mode: writing mode, 'w' overwrite every time, 'a' append to an existing file
    :return: N/A
    """
    # try:
    file = open(filename, mode)
    file.write(fcontent)
    file.close()


def update_config(config, index, val):
    config[index][1] = val
    return config


def write_config(config):
    config_str = ""
    for line in config:
        config_str += line[0] + "=" + line[1] + "\n"
    writefile("myconfig.properties", config_str, 'w', False)


def writefile(filename, fcontent, mode, isprint):
    """
    Write the string content to a file
    copied from
    http://www.pythonforbeginners.com/files/reading-and-writing-files-in-python
    :param filename: name of the file
    :param fcontent: string content to put into the file
    :param mode: writing mode, 'w' overwrite every time, 'a' append to an existing file
    :return: N/A
    """
    # try:
    file = open(filename, mode)
    file.write(fcontent)
    file.close()


def execute_siamese():
    command = ["java", "-jar", "-Xss8g", "siamese-0.0.4-SNAPSHOT.jar", "-cf", "myconfig.properties"]
    # print(command)
    p = Popen(command, stdin=PIPE, stdout=PIPE, stderr=PIPE)
    output, err = p.communicate()
    print(output)
    print(err)


def plot_hist(list):
    f = plt.figure()
    plt.hist(list, bins=1000,log=True)
    # plt.boxplot(list)
    plt.title("Distribution of GitHub project stars: 29465 - 1")
    plt.xlabel("Stars")
    plt.ylabel("Frequency (log)")
    # plt.show()
    f.savefig("stars.pdf", bbox_inches='tight')


def analyse_projects(projs, start, end):
    ###
    # for printing histogram
    ###
    print('max: ' + str(projs[0][0]))
    stars_list = list()
    count = 0

    for proj in projs:
        if start >= proj[0] >= end:
            stars_list.append(proj[0])
            count += 1

    print('amount:', count)
    # plot_hist(stars_list)


def main():
    projs = filter_proj_by_stars(sys.argv[1])
    print('total:', len(projs))
    # analyse_projects(projs, 2, 1)

    start = 29465
    end = 200

    for proj in projs:
        if start >= proj[0] >= end:
            print(proj[0], proj[1])
            config = gen_config_template()
            config = update_config(config, 4, sys.argv[2])
            config = update_config(config, 5, proj[1])
            write_config(config)
            execute_siamese()
            input("Press Enter to continue...")
        # break


main()