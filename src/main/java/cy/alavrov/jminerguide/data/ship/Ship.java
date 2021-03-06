/*
 * Copyright (c) 2015, Andrey Lavrov <lavroff@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package cy.alavrov.jminerguide.data.ship;

import cy.alavrov.jminerguide.log.JMGLogger;
import org.jdom2.Element;

/**
 * Assembled ship with a pilot.
 * @author Andrey Lavrov <lavroff@gmail.com>
 */
public class Ship {    
    private String name;
    
    private Hull hull;
    
    private Turret turret;
    private int turretCount;
    private MiningCrystalLevel miningCrystal;
    
    private HarvestUpgrade harvestUpgrade;
    private int harvestUpgradeCount;
    
    private MiningDrone drone;
    private int droneCount;
    
    private Rig rig1;
    private Rig rig2;
    private Rig rig3;
    
    /**
     * Last selected mining turret. 
     * Used to preserve turret selection between turret types.
     */
    private Turret lastMiningTurret = Turret.MINERI;
    
    /**
     * Last selected mining turret. 
     * Used to preserve turret selection between turret types.
     */
    private Turret lastStripTurret = Turret.STRIPMINERI;
    
    public Ship(String name) {
        this.name = name;
        hull = Hull.VENTURE;
        turret = Turret.MINERI;
        turretCount = hull.getMaxTurrets();
        miningCrystal = MiningCrystalLevel.NOTHING;
        harvestUpgrade = HarvestUpgrade.MININGI;
        harvestUpgradeCount = hull.getMaxUpgrades();      
        drone = MiningDrone.NOTHING;
        droneCount = 0;
        rig1 = Rig.NOTHING;
        rig2 = Rig.NOTHING;
        rig3 = Rig.NOTHING;        
    }
    
    public Ship(Element root) throws Exception {
        String newName = root.getChildText("name");
        if (newName == null) newName = "Unnamed Ship "+System.nanoTime();
        name = newName;
        
        try {
            int hullID = root.getChild("hull").getAttribute("id").getIntValue();
            Hull newHull = Hull.hullsMap.get(hullID);
            hull = (newHull == null) ? Hull.VENTURE : newHull;
        } catch (NullPointerException e) {
            JMGLogger.logWarning("Unable to load hull", e);
            hull = Hull.VENTURE;
        }
                        
        Element turretElem = root.getChild("turret");
        // instead of multiple checks for null, we'll make few NullPointerException
        // catch-alls. While it's not that beautiful, we'll have less code that way.
        
        try {            
            int turrID = turretElem.getAttribute("id").getIntValue();
            Turret newTurret = Turret.turretsMap.get(turrID);
            if (hull.isUsingStripMiners()) {
                if (newTurret == null || 
                        newTurret.getTurretType() == TurretType.GASHARVESTER || 
                        newTurret.getTurretType() == TurretType.MININGLASER) {
                    newTurret = Turret.STRIPMINERI;
                }                
            } else {
                if (newTurret == null || 
                        newTurret.getTurretType() == TurretType.STRIPMINER || 
                        newTurret.getTurretType() == TurretType.ICEHARVESTER) {
                    newTurret = Turret.MINERI;
                }
            }
            
            turret = newTurret;
        } catch (NullPointerException e) {
            JMGLogger.logWarning("Unable to load turret", e);
            if (hull.isUsingStripMiners()) {
                turret = Turret.STRIPMINERI;
            } else {
                turret = Turret.MINERI;
            }
        }

        try {
            int newTurrCount = turretElem.getAttribute("count").getIntValue();
            if (newTurrCount < 0 ) newTurrCount = 0;
            if (newTurrCount > hull.getMaxTurrets()) newTurrCount = hull.getMaxTurrets();
            turretCount = newTurrCount;
        } catch (NullPointerException e) {
            JMGLogger.logWarning("Unable to load turret count", e);
            turretCount = hull.getMaxTurrets();
        }
        
        // as logic will ignore crystals for wrong type of turret, we may just load
        // it regardless. As a side effect, that will allow us not reset crystal
        // selection.
        
        try {
            int crysID = turretElem.getAttribute("crystal").getIntValue();    
            MiningCrystalLevel newCrystal = MiningCrystalLevel.crystalLevelsMap.get(crysID);
            miningCrystal = (newCrystal == null) ? MiningCrystalLevel.NOTHING : newCrystal;
        } catch (NullPointerException e) {
            JMGLogger.logWarning("Unable to load turret crystal", e);
            miningCrystal = MiningCrystalLevel.NOTHING;
        }
        
        Element upgradeElem = root.getChild("upgrade");
        // same catch-all logic here.
        
        try {
            int upgID = upgradeElem.getAttribute("id").getIntValue();    
            HarvestUpgrade newUpg = HarvestUpgrade.upgradesMap.get(upgID);
            harvestUpgrade = (newUpg == null) ? HarvestUpgrade.NOTHING : newUpg;
        } catch (NullPointerException e) {
            JMGLogger.logWarning("Unable to load harvest upgrade", e);
            harvestUpgrade = HarvestUpgrade.NOTHING;
        }
        
        try {
            int newUpgCount = upgradeElem.getAttribute("count").getIntValue();
            if (newUpgCount < 0 ||  harvestUpgrade == HarvestUpgrade.NOTHING) newUpgCount = 0;
            if (newUpgCount > hull.getMaxUpgrades()) newUpgCount = hull.getMaxUpgrades();
            harvestUpgradeCount = newUpgCount;
        } catch (NullPointerException e) {
            JMGLogger.logWarning("Unable to load upgrade count", e);
            harvestUpgradeCount = (harvestUpgrade == HarvestUpgrade.NOTHING) ? 0 : hull.getMaxUpgrades();
        }
        
        Element droneElem = root.getChild("drone");
        // same catch-all logic here.
        
        int droneBandwidth = hull.getDroneBandwidth();
        
        if (droneBandwidth > 0) {
            try {
                int droneID = droneElem.getAttribute("id").getIntValue();    
                MiningDrone newDrone = MiningDrone.dronesMap.get(droneID);
                drone = (newDrone == null) ? MiningDrone.NOTHING : newDrone;
            } catch (NullPointerException e) {
                JMGLogger.logWarning("Unable to load drone", e);
                drone = MiningDrone.NOTHING;
            }

            int maxDroneCount;
            
            if (drone == MiningDrone.NOTHING) {
                maxDroneCount = 0;
            } else {
                maxDroneCount = droneBandwidth / drone.getBandwidth();
                if (maxDroneCount > 5) maxDroneCount = 5;
            }

            try {
                int newDroneCount = droneElem.getAttribute("count").getIntValue();
                if (newDroneCount < 0 ) newDroneCount = 0;
                if (newDroneCount > maxDroneCount) newDroneCount = maxDroneCount;
                droneCount = newDroneCount;
            } catch (NullPointerException e) {
                JMGLogger.logWarning("Unable to load drone count", e);
                harvestUpgradeCount = maxDroneCount;
            }
        } else {
            drone = MiningDrone.NOTHING;
            droneCount = 0;
        }
        
        Element rigsElem = root.getChild("rigs");
        // same catch-all logic here.
        
        try {
            int rig1ID = rigsElem.getAttribute("rig1id").getIntValue();    
            Rig newRig = Rig.rigsMap.get(rig1ID);
            if (newRig == null) newRig = Rig.NOTHING;
            if (!hull.isMediumHull() && newRig.isStrictlyMedium()) newRig = Rig.NOTHING;
            rig1 = newRig;
            
            int rig2ID = rigsElem.getAttribute("rig2id").getIntValue();    
            newRig = Rig.rigsMap.get(rig2ID);
            if (newRig == null) newRig = Rig.NOTHING;
            if (rig1.getCalibrationCost() + newRig.getCalibrationCost() > 400) newRig = Rig.NOTHING;
            if (!hull.isMediumHull() && newRig.isStrictlyMedium()) newRig = Rig.NOTHING;
            rig2 = newRig;
            
            if (hull.getRigSlots() > 2) {
                int rig3ID = rigsElem.getAttribute("rig2id").getIntValue();    
                newRig = Rig.rigsMap.get(rig3ID);
                if (newRig == null) newRig = Rig.NOTHING;
                if (rig1.getCalibrationCost() + rig2.getCalibrationCost() + 
                        newRig.getCalibrationCost() > 400) newRig = Rig.NOTHING;
                if (!hull.isMediumHull() && newRig.isStrictlyMedium()) newRig = Rig.NOTHING;
                rig3 = newRig;
            } else {
                rig3 = Rig.NOTHING;
            }
            
        } catch (NullPointerException e) {
            JMGLogger.logWarning("Unable to load rigs", e);
            
            rig1 = Rig.NOTHING;
            rig2 = Rig.NOTHING;
            rig3 = Rig.NOTHING;
        }        
    }
    
    public synchronized Element getXMLElement() {    
        Element root = new Element("ship");        

        root.addContent(new Element("name").setText(name));

        root.addContent(new Element("hull").setAttribute("id", String.valueOf(hull.getID())));

        root.addContent(new Element("turret")
                .setAttribute("id", String.valueOf(turret.getID()))
                .setAttribute("count", String.valueOf(turretCount))
                .setAttribute("crystal", String.valueOf(miningCrystal.getID()))
        );

        root.addContent(new Element("upgrade")
                .setAttribute("id", String.valueOf(harvestUpgrade.getID()))
                .setAttribute("count", String.valueOf(harvestUpgradeCount))
        );

        root.addContent(new Element("drone")
                .setAttribute("id", String.valueOf(drone.getID()))
                .setAttribute("count", String.valueOf(droneCount))
        );

        root.addContent(new Element("rigs")
                .setAttribute("rig1id", String.valueOf(rig1.getID()))
                .setAttribute("rig2id", String.valueOf(rig2.getID()))
                .setAttribute("rig3id", String.valueOf(rig3.getID()))
        );

        return root;     
    }
    
    /**
     * Sets a ship's hull. As a new hull can have different stats, this can
     * change maximum number of turrets, upgrades, drones and/or rig composition.
     * @param newHull 
     */
    public synchronized void setHull(Hull newHull) {
        if (newHull == null) return;
        
        hull = newHull;
        if (turretCount > hull.getMaxTurrets()) {
            turretCount = hull.getMaxTurrets();
        }

        // let's preserve turret selection, if we have to change turret type.
        if (hull.isUsingStripMiners()) {
            if (turret.getTurretType() == TurretType.GASHARVESTER || 
                turret.getTurretType() == TurretType.MININGLASER) {
                lastMiningTurret = turret;
                turret = lastStripTurret;
            }
        } else {
            if (turret.getTurretType() == TurretType.STRIPMINER || 
                turret.getTurretType() == TurretType.ICEHARVESTER) {
                lastStripTurret = turret;
                turret = lastMiningTurret;
            }
        }

        if (this.harvestUpgradeCount > hull.getMaxUpgrades()) {
            this.harvestUpgradeCount = hull.getMaxUpgrades();
        }

        // upgrade type won't change.

        int maxDroneCount  = hull.getDroneBandwidth() / drone.getBandwidth();   
        if (maxDroneCount > 5) maxDroneCount = 5;

        if (droneCount > maxDroneCount) droneCount = maxDroneCount;

        if (!hull.isMediumHull()) {
            if (rig1.isStrictlyMedium()) rig1 = Rig.NOTHING;
            if (rig2.isStrictlyMedium()) rig2 = Rig.NOTHING;
            if (rig3.isStrictlyMedium()) rig3 = Rig.NOTHING;
        }
    }
    
    public synchronized Hull getHull() {
        return hull;
    }
    
    /**
     * Sets type of a turret.
     * @param newTurret 
     */
    public synchronized void setTurret(Turret newTurret) {
        if (turret == null) return;
        
        if (hull.isUsingStripMiners()) {
            if (turret.getTurretType() == TurretType.GASHARVESTER || 
                turret.getTurretType() == TurretType.MININGLASER) {
                return;
            }
        } else {
            if (turret.getTurretType() == TurretType.STRIPMINER || 
                turret.getTurretType() == TurretType.ICEHARVESTER) {
                return;
            }
        }

        turret = newTurret;
    }
    
    public synchronized Turret getTurret() {
        return turret;
    }
    
    /**
     * Sets, how much turrets we have installed on a ship.
     * Can't exceed max turrets on a hull.
     * @param count 
     */
    public synchronized void setTurrentCount(int count) {
        if (count < 0) count = 0;
        if (count > hull.getMaxTurrets()) count = hull.getMaxTurrets();
        turretCount = count;
    }
    
    public synchronized int getTurretCount() {
        return turretCount;
    }
    
    /**
     * Sets turret's crystal. Can set crystals on turrets that doesn't use
     * them, this is intentional (calculations ignore them anyway in that case).
     * @param lvl 
     */
    public synchronized void setTurretCrystal(MiningCrystalLevel lvl) {
        if (lvl == null) return;
        miningCrystal = lvl;
    }
    
    public synchronized MiningCrystalLevel getTurretCrystal() {
        return miningCrystal;
    }
    
    /**
     * Sets harvest upgrade type.
     * @param newUpg 
     */
    public synchronized void setHarvestUpgrade(HarvestUpgrade newUpg) {
        if (newUpg == null) return;
        harvestUpgrade = newUpg;
    }
    
    public synchronized HarvestUpgrade getHarvestUpgrade() {
        return harvestUpgrade;
    }
    
    /**
     * Sets how much harvest upgrades we have installed on a ship.
     * Can't exceed max upgrades (i.e. lowslots) on a ship.
     * @param count 
     */
    public synchronized void setHarvestUpgradeCount(int count) {
        if (count < 0) count = 0;
        if (count > hull.getMaxUpgrades()) count = hull.getMaxUpgrades();
        harvestUpgradeCount = count;
    }
    
    public synchronized int getHarvestUpgradeCount() {
        return harvestUpgradeCount;
    }
    
    public synchronized void setDrone(MiningDrone newDrone) {
        if (newDrone == null) return;
        
        drone = newDrone;
        int maxDroneCount = hull.getDroneBandwidth() / drone.getBandwidth();
        if (maxDroneCount > 5) maxDroneCount = 5;
        if (droneCount > maxDroneCount) droneCount = maxDroneCount;
    }
    
    public synchronized MiningDrone getDrone() {
        return drone;
    }
    
    public synchronized void setDroneCount(int count) {     
        int maxDroneCount = hull.getDroneBandwidth() / drone.getBandwidth();
        if (maxDroneCount > 5) maxDroneCount = 5;
        if (count > maxDroneCount) count = maxDroneCount;
        droneCount = count;
    }
    
    public synchronized int getDroneCount() {
        return droneCount;
    }
    
    public synchronized int getMaxDrones() {
        int maxDroneCount = hull.getDroneBandwidth() / drone.getBandwidth();
        if (maxDroneCount > 5) maxDroneCount = 5;
        return maxDroneCount;
    }
    
    public synchronized Rig getRig1() {
        return rig1;
    }
    
    public synchronized Rig getRig2() {
        return rig2;
    }
    
    public synchronized Rig getRig3() {
        return rig3;
    }
    
    /**
     * Sets rig in the slot 1. Can fail due to bad rig size or calibration overuse.
     * @param newRig
     * @return false if failed.
     */
    public synchronized boolean setRig1(Rig newRig) {
        if (newRig == null) return false;
        
        if (!hull.isMediumHull() && newRig.isStrictlyMedium()) return false;
        if (rig2.getCalibrationCost() + rig3.getCalibrationCost() + 
                newRig.getCalibrationCost() > 400) return false;
        rig1 = newRig;
        return true;
    }
    
    /**
     * Sets rig in the slot 2. Can fail due to bad rig size or calibration overuse.
     * @param newRig
     * @return false if failed.
     */
    public synchronized boolean setRig2(Rig newRig) {
        if (newRig == null) return false;
        
        if (!hull.isMediumHull() && newRig.isStrictlyMedium()) return false;
        if (rig1.getCalibrationCost() + rig3.getCalibrationCost() + 
                newRig.getCalibrationCost() > 400) return false;
        rig2 = newRig;
        return true;
    }
    
    /**
     * Sets rig in the slot 3. Can fail due to bad rig size or calibration overuse.
     * @param newRig
     * @return false if failed.
     */
    public synchronized boolean setRig3(Rig newRig) {
        if (newRig == null) return false;
        
        if (!hull.isMediumHull() && newRig.isStrictlyMedium()) return false;
        if (rig1.getCalibrationCost() + rig2.getCalibrationCost() + 
                newRig.getCalibrationCost() > 400) return false;
        rig3 = newRig;
        return true;
    }
    
    public synchronized String getName() {
        return name;
    }
    
    public synchronized void setName(String name) {
        if (name == null) return;
        this.name = name;
    }
    
    @Override
    public synchronized String toString() {
        return name;
    }
}
