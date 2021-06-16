package kr.stteam.TwtRoute.domain;


public class TwtTaskItem {

    public String task_id;
    public double x;
    public double y;
    public int index; // input index
    public String poi_name;
    public int order; // job order, input order after processing.

    public double mx;
    public double my;
    public int tw_req;
    public double tw_req_start;
    public double tw_req_end;
    public double tw_end;
    public double tm_arrival;
    public double sum_cost;
    public double tm_end;
    public double tm_last_transfer;
    public double last_cost;
    public double tm_service;
    public double tbl_distance;
    public double tbl_duration;
    public double tbl_weight;
    public double last_route_distance;
    public double last_route_duration;
    public double last_route_weight;
    public taskitem_type task_type;

    public enum taskitem_type {
        job_start(0x0001), job_end(0x0002), job_delivery(0x0004), job_unassigned(0x0008);

        private int value;

        taskitem_type(int value) {
            this.value = value;
        }
    };

    @Override
    public String toString() {
        return String.format("[%d] %d (%.7f, %.7f)", index, order, x, y);
    }

}
