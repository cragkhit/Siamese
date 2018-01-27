import pandas as pd
import os


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


def main():

    groundtruth = pd.read_csv('../results/bcb_groundtruth_qr-25-75-10-new.csv', sep=',', header=None)
    gt = groundtruth[1:][4].tolist()
    # print(len(gt), gt)
    # exit()

    data = pd.read_csv('../results/bcb_search_results_qr-25-75-10-new_copied.csv', sep=',', header=None)
    # print(data)
    # exit()

    QUERIES = 142
    RESULTSIZE = 50
    OUTFILE = '../results/bcb_precision_qr-25-75-10-new.csv'

    writefile(OUTFILE, 'r,10-prec,r-prec,tp,fp,t1,t2,t3\n', 'a', False)
    precsum = [0, 0, 0, 0, 0, 0, 0]

    for i in range(1, (QUERIES + 1)):
        start = i + (i - 1) * RESULTSIZE
        end = i * RESULTSIZE + i - 1
        print(start, end)
        q1dat = data.loc[start: end][7]
        t1 = t2 = t3 = fp = 0
        sum10 = 0
        # print(i)
        limit = int(gt[i - 1])
        # reciprocal rank
        rr = limit

        for idx, d in enumerate(q1dat):

            if idx < limit:
                if d == 'T1' or d == 'T1*' or d == 'T1*J' or d == 'MT1' or d == 'MT1*' or d == 'MT1*J':
                    t1 += 1
                    if idx < 10:
                        sum10 += 1
                    if rr == limit:
                        rr = idx + 1
                elif d == 'T2' or d == 'T2*' or d == 'T2*J' or d == 'MT2' or d == 'MT2*' or d == 'MT2*J':
                    t2 += 1
                    if idx < 10:
                        sum10 += 1
                    if rr == limit:
                        rr = idx + 1
                elif d == 'T3' or d == 'T3*' or d == 'T3*J'  or d == 'MT3' or d == 'MT3*' or d == 'MT3*J':
                    t3 += 1
                    if idx < 10:
                        sum10 += 1
                    if rr == limit:
                        rr = idx + 1
                elif d == 'F' or d == 'MF' or d == 'MF*' or d == 'MF*J':
                    fp += 1
            else:
                break

        tenprec = sum10/10
        rprec = (t1 + t2 + t3)/limit
        precsum[0] += 1/rr
        precsum[1] += tenprec
        precsum[2] += rprec

        out = str(limit) + ','
        out = out + str(rr) + ','
        out = out + str(tenprec) + ','
        out = out + str(rprec) \
              + ',' + str(t1 + t2 + t3) + ',' + str(fp) \
              + ',' + str(t1) + ',' + str(t2) + ',' + str(t3) + '\n'

        writefile(OUTFILE, out, 'a', True)

    print(precsum[0]/QUERIES, precsum[1]/QUERIES, precsum[2]/QUERIES)


main()