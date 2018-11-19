from scipy.optimize import least_squares
from scipy.spatial.distance import euclidean

def predictPosition(dist_dict):
    a = """1 0.2,0.2
2 7.5,12
3 8.5,23
4 5.5,20
5 13,27.5
6 5.5,31.5"""
    a = [i.split(" ") for i in a.split("\n")]
    anchors = {int(k):v for [k,v] in a}
    for k in anchors:
        anchors[k] = tuple(float(a) for a in anchors[k].split(","))

    def compute_residuals(x, *args, **kwargs):
        anchor_dict = kwargs["anchor_dict"]
        distance_dict = kwargs["distance_dict"]
        return [euclidean(x,anchor_dict[k])-distance_dict[k] for k in range(1,7)]

    return least_squares(fun = compute_residuals, kwargs = {"anchor_dict": anchors, "distance_dict":dist_dict}, x0 = (8,24)).x



if __name__ == '__main__':
    print(predictPosition({1:4, 2:7, 3:13.3, 4:10.5, 5:20, 6:21.5}))

