// Lightweight local "database" so the React app is fully demoable
// before the Spring Boot backend endpoints exist. Swap USE_MOCK to
// false (see services.js) once the real API is ready — every service
// function keeps the same signature either way, so pages never change.

const DB_KEY = 'awb_db_v2'

const seed = () => ({
  superAdmin: { username: 'admin', password: 'admin@123', email: 'admin@hydrobill.com' },
  invitationTokens: [
    {
      id: 'tok_demo',
      token: 'demo-invitation-token-123',
      fullName: 'Priya Sharma',
      email: 'priya@gmail.com',
      phone: '9876543210',
      flatNumber: 'A-103',
      blockNumber: 'A',
      buildingId: 'bld_1002',
      buildingName: 'Green Valley Apartments',
      expiryDate: Date.now() + 1000 * 60 * 60 * 48,
      isUsed: false
    }
  ],
  buildings: [
    {
      id: 'bld_1001',
      buildingName: 'Palm Residency',
      ownerName: 'K. Ramesh',
      address: '12-3-45, Brodipet',
      location: 'Guntur, AP',
      phone: '9876543210',
      email: 'ramesh.owner@example.com',
      username: 'ramesh.owner',
      password: 'owner@123',
      status: 'PENDING',
      createdAt: Date.now() - 1000 * 60 * 60 * 24 * 2
    },
    {
      id: 'bld_1002',
      buildingName: 'Green Valley Apartments',
      ownerName: 'S. Lakshmi',
      address: '4-2-19, Lakshmipuram',
      location: 'Vijayawada, AP',
      phone: '9123456780',
      email: 'lakshmi.owner@example.com',
      username: 'lakshmi.owner',
      password: 'owner@123',
      status: 'APPROVED',
      createdAt: Date.now() - 1000 * 60 * 60 * 24 * 20
    }
  ],
  residents: [
    {
      id: 'res_2001',
      buildingId: 'bld_1002',
      flatNumber: 'A-101',
      name: 'Priya Nair',
      username: 'priya.a101',
      password: 'resident@123',
      email: 'priya.nair@example.com',
      phone: '9988776655',
      flatArea: 1200,
      isMetered: true,
      alertThresholdKl: 20,
      invitationStatus: 'ACCEPTED',
      createdAt: Date.now() - 1000 * 60 * 60 * 24 * 18
    },
    {
      id: 'res_2002',
      buildingId: 'bld_1002',
      flatNumber: 'A-102',
      name: 'Arjun Rao',
      username: 'arjun.a102',
      password: 'resident@123',
      email: 'arjun.rao@example.com',
      phone: '9911223344',
      flatArea: 1000,
      isMetered: false, // Unmetered household fallback test
      alertThresholdKl: 20,
      invitationStatus: 'ACCEPTED',
      createdAt: Date.now() - 1000 * 60 * 60 * 24 * 3
    }
  ],
  tariffs: [
    {
      id: 'trf_1',
      buildingId: 'bld_1002',
      tiers: [
        { tierName: 'Base Tier (0-10 kL)', minKl: 0, maxKl: 10, ratePerKl: 10, fixedCharge: 0 },
        { tierName: 'High Usage (>10 kL)', minKl: 10, maxKl: null, ratePerKl: 15, fixedCharge: 0 }
      ]
    }
  ],
  bulkPurchases: [
    {
      id: 'pur_1',
      buildingId: 'bld_1002',
      billingCycleId: 'bcy_101',
      sourceType: 'Tanker Delivery',
      supplierName: 'Aqua Pure Tankers',
      purchaseDate: '2026-07-15',
      purchasedVolumeKl: 50,
      totalCost: 5000,
      unitCostPerKl: 100,
      notes: 'Emergency water supply for July'
    }
  ],
  billingCycles: [
    {
      id: 'bcy_101',
      buildingId: 'bld_1002',
      month: 'July',
      year: 2026,
      status: 'FINALIZED',
      openedAt: Date.now() - 1000 * 60 * 60 * 24 * 30,
      finalizedAt: Date.now() - 1000 * 60 * 60 * 24 * 2
    }
  ],
  meterReadings: [
    {
      id: 'mtr_3001',
      residentId: 'res_2001',
      buildingId: 'bld_1002',
      month: 'June',
      year: 2026,
      previousReading: 95,
      currentReading: 120,
      usage: 25,
      createdAt: Date.now() - 1000 * 60 * 60 * 24 * 30
    },
    {
      id: 'mtr_3002',
      residentId: 'res_2001',
      buildingId: 'bld_1002',
      month: 'July',
      year: 2026,
      previousReading: 120,
      currentReading: 142,
      usage: 22,
      createdAt: Date.now() - 1000 * 60 * 60 * 24 * 2
    }
  ],
  bills: [
    {
      id: 'bill_4001',
      residentId: 'res_2001',
      buildingId: 'bld_1002',
      meterReadingId: 'mtr_3001',
      month: 'June',
      year: 2026,
      usage: 25,
      amount: 900,
      status: 'PAID',
      generatedAt: Date.now() - 1000 * 60 * 60 * 24 * 29,
      paidAt: Date.now() - 1000 * 60 * 60 * 24 * 27,
      paymentId: 'pay_mock_11'
    }
  ],
  invoices: [
    {
      id: 'inv_5001',
      invoiceNumber: 'INV-202607-A101',
      billingCycleId: 'bcy_101',
      residentId: 'res_2001',
      buildingId: 'bld_1002',
      flatNumber: 'A-101',
      residentName: 'Priya Nair',
      billingPeriod: 'July 2026',
      meteredConsumptionKl: 22,
      flatAreaSqft: 1200,
      isMetered: true,
      baseTieredCharge: 280, // (10x10) + (12x15) = 100 + 180 = 280
      allocatedWaterProcurementCharge: 2500, // 50% metered procurement share
      sharedAreaCharge: 150,
      adjustments: 0,
      totalAmount: 2930,
      status: 'PENDING',
      generatedAt: Date.now() - 1000 * 60 * 60 * 24 * 2,
      dueDate: '2026-08-15',
      paidAt: null,
      paymentId: null,
      breakdown: {
        tier1Portion: '10 kL @ ₹10 = ₹100',
        tier2Portion: '12 kL @ ₹15 = ₹180',
        procurementNote: 'Metered share of 50 kL bulk tanker cost (₹5,000)',
        sharedNote: 'Common garden & cleaning allocation'
      }
    }
  ],
  notifications: [
    {
      id: 'ntf_6001',
      forRole: 'RESIDENT',
      forId: 'res_2001',
      alertType: 'HIGH_CONSUMPTION',
      severity: 'WARNING',
      title: '⚠ HIGH WATER CONSUMPTION',
      message: 'Your current water consumption is 18.5 kL. You are currently in the Medium Consumption tier (11–25 kL). Please monitor your water usage.',
      notificationType: 'HIGH_CONSUMPTION',
      tariffTier: 'Medium Consumption Tier (11–25 kL)',
      currentConsumption: 18.5,
      averageConsumption: 14.2,
      standardDeviation: 2.1,
      createdAt: Date.now() - 1000 * 60 * 60 * 2,
      isRead: false,
      isResolved: false
    },
    {
      id: 'ntf_6002',
      forRole: 'RESIDENT',
      forId: 'res_2001',
      alertType: 'POSSIBLE_WATER_LEAK',
      severity: 'CRITICAL',
      title: '🚨 POSSIBLE WATER LEAK DETECTED',
      message: '🚨 Possible Water Leak Detected\n\nYour current water consumption (38.0 kL) is significantly higher than your normal household usage (Average: 17.0 kL).\n\nPlease check your taps, pipes, tanks, and other water connections for possible leakage.',
      notificationType: 'POSSIBLE_WATER_LEAK',
      tariffTier: 'High Consumption Tier (26+ kL)',
      currentConsumption: 38.0,
      averageConsumption: 17.0,
      standardDeviation: 3.5,
      createdAt: Date.now() - 1000 * 60 * 60 * 12,
      isRead: false,
      isResolved: false
    },
    {
      id: 'ntf_6003',
      forRole: 'RESIDENT',
      forId: 'res_2001',
      alertType: 'CRITICAL_HIGH_CONSUMPTION',
      severity: 'CRITICAL',
      title: '⚠ CRITICAL HIGH CONSUMPTION',
      message: 'Your current water consumption is 30.0 kL. You have entered the High Consumption tier (26+ kL). Please reduce usage to prevent high charges.',
      notificationType: 'CRITICAL_HIGH_CONSUMPTION',
      tariffTier: 'High Consumption Tier (26+ kL)',
      currentConsumption: 30.0,
      averageConsumption: 16.5,
      standardDeviation: 2.8,
      createdAt: Date.now() - 1000 * 60 * 60 * 48,
      isRead: true,
      isResolved: true,
      resolvedAt: Date.now() - 1000 * 60 * 60 * 24
    }
  ],
  resetTokens: []
})

export function loadDb() {
  const raw = localStorage.getItem(DB_KEY)
  if (!raw) {
    const fresh = seed()
    localStorage.setItem(DB_KEY, JSON.stringify(fresh))
    return fresh
  }
  try {
    return JSON.parse(raw)
  } catch {
    const fresh = seed()
    localStorage.setItem(DB_KEY, JSON.stringify(fresh))
    return fresh
  }
}

export function saveDb(db) {
  localStorage.setItem(DB_KEY, JSON.stringify(db))
}

export function resetDb() {
  const fresh = seed()
  saveDb(fresh)
  return fresh
}

export const uid = (prefix) => `${prefix}_${Math.random().toString(36).slice(2, 9)}`

export const delay = (ms = 350) => new Promise((r) => setTimeout(r, ms))
