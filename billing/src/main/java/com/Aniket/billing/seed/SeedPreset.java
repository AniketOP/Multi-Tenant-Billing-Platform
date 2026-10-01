package com.Aniket.billing.seed;

public enum SeedPreset {

    SMALL(5, 50, 12),
    MEDIUM(20, 200, 12),
    LARGE(50, 500, 12);

    private final int tenants;
    private final int unitsPerTenant;
    private final int months;

    SeedPreset(int tenants, int unitsPerTenant , int months){
        this.tenants = tenants;
        this.unitsPerTenant = unitsPerTenant;
        this.months = months;
    }

    public int getTenants(){return tenants;}
    public int getUnitsPerTenant() { return unitsPerTenant; }
    public int getMonths() { return months; }


}
