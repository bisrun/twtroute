/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.RouteVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class UpdateVehicleDependentPracticalTimeWindows implements RouteVisitor, StateUpdater {

    @Override
    public void visit(VehicleRoute route) {
        begin(route);
        
        // 도착 activity 부터 visit 함수 실행 (reverseA.. Iter..)
        Iterator<TourActivity> revIterator = route.getTourActivities().reverseActivityIterator(); 
        while (revIterator.hasNext()) {
            visit(revIterator.next());
        }
        finish();
    }

    public interface VehiclesToUpdate {

        Collection<Vehicle> get(VehicleRoute route);

    }

    private VehiclesToUpdate vehiclesToUpdate = route -> Arrays.asList(route.getVehicle());

    private final StateManager stateManager;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private VehicleRoute route;

    private double[] latest_arrTimes_at_prevAct;

    private Location[] location_of_prevAct;

    private Collection<Vehicle> vehicles;

    public UpdateVehicleDependentPracticalTimeWindows(StateManager stateManager, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.stateManager = stateManager;
        this.transportCosts = tpCosts;
        this.activityCosts = activityCosts;
        latest_arrTimes_at_prevAct = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        location_of_prevAct = new Location[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
    }

    public void setVehiclesToUpdate(VehiclesToUpdate vehiclesToUpdate) {
        this.vehiclesToUpdate = vehiclesToUpdate;
    }


    public void begin(VehicleRoute route) {
        this.route = route;
        vehicles = vehiclesToUpdate.get(route);
        for (Vehicle vehicle : vehicles) {
        	
        	//[by hsb] input latest arrival time of this vehicle.
            latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = vehicle.getLatestArrival();
            
            //[by hsb] input latest arrival 차고(depot position) of this vehicle.
            Location location = vehicle.getEndLocation();
            
            //[by hsb] 차량이 차고로 돌아가지 않으면, 마지막 배송지점 입력
            if(!vehicle.isReturnToDepot()){
                location = route.getEnd().getLocation();
            }
            location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = location;
        }
    }


    public void visit(TourActivity activity) {
        for (Vehicle vehicle : vehicles) {
            double latestArrTimeAtPrevAct = latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()];// 이전 지점(activity) 까지의 도착시간
            Location prevLocation = location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()];// 이전 지점(activity) 위치
            
            // 허용 가능한 도착시간 = N'th 지점 도착시간 - ( activity.pos --> N'th.pos 시간 ) - (duration time i.e service time)
            double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transportCosts.getBackwardTransportTime(activity.getLocation(), prevLocation,
                latestArrTimeAtPrevAct, route.getDriver(), vehicle) - activityCosts.getActivityDuration(activity, latestArrTimeAtPrevAct, route.getDriver(), route.getVehicle());
            
            // activity.service.getTimeWindow().getEnd()-- last end time in tw of activity
            double latestArrivalTime = Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);
            
            // 지점도착시간 < 지점출발시간 : 도착-->서비스-->출발 이므로 ,  
            if (latestArrivalTime < activity.getTheoreticalEarliestOperationStartTime()) {
                stateManager.putTypedInternalRouteState(route, vehicle, InternalStates.SWITCH_NOT_FEASIBLE, true);
            }
            stateManager.putInternalTypedActivityState(activity, vehicle, InternalStates.LATEST_OPERATION_START_TIME, latestArrivalTime);
            latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = latestArrivalTime;
            location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = activity.getLocation();
        }
    }


    public void finish() {
    }

}

