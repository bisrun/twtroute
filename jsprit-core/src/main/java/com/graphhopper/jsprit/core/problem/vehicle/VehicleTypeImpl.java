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
package com.graphhopper.jsprit.core.problem.vehicle;


import com.graphhopper.jsprit.core.problem.Capacity;

/**
 * Implementation of {@link VehicleType}.
 * <p>
 * <p>Two vehicle-types are equal if they have the same typeId.
 *
 * @author schroeder
 */
public class VehicleTypeImpl implements VehicleType {

    /**
     * CostParameter consisting of fixed cost parameter, time-based cost parameter and distance-based cost parameter.
     *
     * @author schroeder
     */
    public static class VehicleCostParams {


        public static VehicleTypeImpl.VehicleCostParams newInstance(double fix, double perTimeUnit, double perDistanceUnit) {
            return new VehicleCostParams(fix, perTimeUnit, perDistanceUnit);
        }

        public final double fix;

        public final double perTransportTimeUnit;
        public final double perDistanceUnit;
        public final double perWaitingTimeUnit;
        public final double perServiceTimeUnit;

        private VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit) {
            super();
            this.fix = fix;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = 0.;
            this.perServiceTimeUnit = 0.;
        }

        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit) {
            this.fix = fix;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = 0.;
        }

        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit, double perServiceTimeUnit) {
            this.fix = fix;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = perServiceTimeUnit;
        }

        @Override
        public String toString() {
            return "[fixed=" + fix + "][perTime=" + perTransportTimeUnit + "][perDistance=" + perDistanceUnit + "][perWaitingTimeUnit=" + perWaitingTimeUnit + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VehicleCostParams)) return false;

            VehicleCostParams that = (VehicleCostParams) o;

            if (Double.compare(that.fix, fix) != 0) return false;
            if (Double.compare(that.perTransportTimeUnit, perTransportTimeUnit) != 0) return false;
            if (Double.compare(that.perDistanceUnit, perDistanceUnit) != 0) return false;
            if (Double.compare(that.perWaitingTimeUnit, perWaitingTimeUnit) != 0) return false;
            return Double.compare(that.perServiceTimeUnit, perServiceTimeUnit) == 0;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(fix);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perTransportTimeUnit);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perDistanceUnit);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perWaitingTimeUnit);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perServiceTimeUnit);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    /**
     * Builder that builds the vehicle-type.
     *
     * @author schroeder
     */
    public static class Builder {


        public static VehicleTypeImpl.Builder newInstance(String id) {
            if (id == null) throw new IllegalArgumentException();
            return new Builder(id);
        }

        private String id;
        private double maxVelo = Double.MAX_VALUE;
        /**
         * default cost values for default vehicle type
         */
        private double fixedCost = 0.0;
        private double perDistance = 0.1;//[by hsb] ori =1.0
        private double perTime = 1.0; //[by hsb] ori=0.0
        private double perWaitingTime = 0.0;
        private double perServiceTime = 0.0;

        private String profile = "car";

        private Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        private Capacity capacityDimensions = null;

        private boolean dimensionAdded = false;

        private Object userData;

        private Builder(String id) {
            this.id = id;
        }


        /**
         * Sets user specific domain data associated with the object.
         *
         * <p>
         * The user data is a black box for the framework, it only stores it,
         * but never interacts with it in any way.
         * </p>
         *
         * @param userData
         *            any object holding the domain specific user data
         *            associated with the object.
         * @return builder
         */
        public Builder setUserData(Object userData) {
            this.userData = userData;
            return this;
        }

        /**
         * Sets the maximum velocity this vehicle-type can go [in meter per
         * seconds].
         *
         * @param inMeterPerSeconds in m/s
         * @return this builder
         * @throws IllegalArgumentException
         *             if velocity is smaller than zero
         */
        public VehicleTypeImpl.Builder setMaxVelocity(double inMeterPerSeconds) {
            if (inMeterPerSeconds < 0.0)
                throw new IllegalArgumentException("The velocity of a vehicle (type) cannot be smaller than zero.");
            this.maxVelo = inMeterPerSeconds;
            return this;
        }

        /**
         * Sets the fixed costs of the vehicle-type.
         * <p>
         * <p>by default it is 0.
         *
         * @param fixedCost fixed cost of vehicle type
         * @return this builder
         * @throws IllegalArgumentException if fixedCost is smaller than zero
         */
        public VehicleTypeImpl.Builder setFixedCost(double fixedCost) {
            if (fixedCost < 0.0) throw new IllegalArgumentException("Fixed costs must not be smaller than zero.");
            this.fixedCost = fixedCost;
            return this;
        }

        /**
         * Sets the cost per distance unit, for instance ??? per meter.
         * <p>
         * <p>by default it is 1.0
         *
         * @param perDistance cost per distance
         * @return this builder
         * @throws IllegalArgumentException if perDistance is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerDistance(double perDistance) {
            if (perDistance < 0.0)
                throw new IllegalArgumentException("Cost per distance must not be smaller than zero.");
            this.perDistance = perDistance;
            return this;
        }

        /**
         * Sets cost per time unit, for instance ??? per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perTime cost per time
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         * @deprecated use .setCostPerTransportTime(..) instead
         */
        @Deprecated
        public VehicleTypeImpl.Builder setCostPerTime(double perTime) {
            if (perTime < 0.0) throw new IllegalArgumentException();
            this.perTime = perTime;
            return this;
        }

        /**
         * Sets cost per time unit, for instance ??? per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perTime cost per time
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerTransportTime(double perTime) {
            if (perTime < 0.0) throw new IllegalArgumentException();
            this.perTime = perTime;
            return this;
        }

        /**
         * Sets cost per waiting time unit, for instance ??? per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perWaitingTime cost per waiting time
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerWaitingTime(double perWaitingTime) {
            if (perWaitingTime < 0.0) throw new IllegalArgumentException();
            this.perWaitingTime = perWaitingTime;
            return this;
        }

        public VehicleTypeImpl.Builder setCostPerServiceTime(double perServiceTime) {
            this.perServiceTime = perServiceTime;
            return this;
        }

        /**
         * Builds the vehicle-type.
         *
         * @return VehicleTypeImpl
         */
        public VehicleTypeImpl build() {
            if (capacityDimensions == null) {
                capacityDimensions = capacityBuilder.build();
            }
            return new VehicleTypeImpl(this);
        }

        /**
         * Adds a capacity dimension.
         *
         * @param dimIndex dimension index
         * @param dimVal dimension value
         * @return the builder
         * @throws IllegalArgumentException if dimVal < 0
         * @throws IllegalArgumentException    if capacity dimension is already set
         */
        public Builder addCapacityDimension(int dimIndex, int dimVal) {
            if (dimVal < 0) throw new IllegalArgumentException("The capacity value must not be negative.");
            if (capacityDimensions != null)
                throw new IllegalArgumentException("Either build your dimension with build your dimensions with " +
                    "addCapacityDimension(int dimIndex, int dimVal) or set the already built dimensions with .setCapacityDimensions(Capacity capacity)." +
                    "You used both methods.");
            dimensionAdded = true;
            capacityBuilder.addDimension(dimIndex, dimVal);
            return this;
        }

        /**
         * Sets capacity dimensions.
         * <p>
         * <p>Note if you use this you cannot use <code>addCapacityDimension(int dimIndex, int dimVal)</code> anymore. Thus either build
         * your dimensions with <code>addCapacityDimension(int dimIndex, int dimVal)</code> or set the already built dimensions with
         * this method.
         *
         * @param capacity capacity of vehicle type
         * @return this builder
         * @throws IllegalArgumentException if capacityDimension has already been added
         */
        public Builder setCapacityDimensions(Capacity capacity) {
            if (dimensionAdded)
                throw new IllegalArgumentException("Either build your dimension with build your dimensions with " +
                    "addCapacityDimension(int dimIndex, int dimVal) or set the already built dimensions with .setCapacityDimensions(Capacity capacity)." +
                    "You used both methods.");
            this.capacityDimensions = capacity;
            return this;
        }

        public Builder setProfile(String profile) {
            this.profile = profile;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleTypeImpl)) return false;

        VehicleTypeImpl that = (VehicleTypeImpl) o;

        if (Double.compare(that.maxVelocity, maxVelocity) != 0) return false;
        if (!typeId.equals(that.typeId)) return false;
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) return false;
        if (!vehicleCostParams.equals(that.vehicleCostParams)) return false;
        return capacityDimensions.equals(that.capacityDimensions);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = typeId.hashCode();
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + vehicleCostParams.hashCode();
        result = 31 * result + capacityDimensions.hashCode();
        temp = Double.doubleToLongBits(maxVelocity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    private final String typeId;

    private final String profile;

    private final VehicleTypeImpl.VehicleCostParams vehicleCostParams;

    private final Capacity capacityDimensions;

    private final double maxVelocity;

    private Object userData;

    /**
     * priv constructor constructing vehicle-type
     *
     * @param builder vehicle type builder
     */
    private VehicleTypeImpl(VehicleTypeImpl.Builder builder) {
        this.userData = builder.userData;
        typeId = builder.id;
        maxVelocity = builder.maxVelo;
        vehicleCostParams = new VehicleCostParams(builder.fixedCost, builder.perTime, builder.perDistance, builder.perWaitingTime, builder.perServiceTime);
        capacityDimensions = builder.capacityDimensions;
        profile = builder.profile;
    }

    /**
     * @return User-specific domain data associated with the vehicle
     */
    @Override
    public Object getUserData() {
        return userData;
    }

    /* (non-Javadoc)
     * @see basics.route.VehicleType#getTypeId()
     */
    @Override
    public String getTypeId() {
        return typeId;
    }

    /* (non-Javadoc)
     * @see basics.route.VehicleType#getVehicleCostParams()
     */
    @Override
    public VehicleTypeImpl.VehicleCostParams getVehicleCostParams() {
        return vehicleCostParams;
    }

    @Override
    public String toString() {
        return "[typeId=" + typeId + "]" +
            "[capacity=" + capacityDimensions + "]" +
            "[costs=" + vehicleCostParams + "]";
    }

    @Override
    public double getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    public Capacity getCapacityDimensions() {
        return capacityDimensions;
    }

    @Override
    public String getProfile() {
        return profile;
    }

}
