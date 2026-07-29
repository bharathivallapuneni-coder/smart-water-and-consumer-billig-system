// Lightweight local "database" so the React app is fully demoable
// before the Spring Boot backend endpoints exist. Swap USE_MOCK to
// false (see services.js) once the real API is ready — every service
// function keeps the same signature either way, so pages never change.

const DB_KEY = 'awb_db_v1'

const seed = () => ({
  superAdmin: { username: 'admin', password: 'admin@123' },
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
      phone: '9988776655',
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
      phone: '9911223344',
      invitationStatus: 'PENDING',
      createdAt: Date.now() - 1000 * 60 * 60 * 24 * 3
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
      currentReading: 145,
      usage: 25,
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
    },
    {
      id: 'bill_4002',
      residentId: 'res_2001',
      buildingId: 'bld_1002',
      meterReadingId: 'mtr_3002',
      month: 'July',
      year: 2026,
      usage: 25,
      amount: 900,
      status: 'PENDING',
      generatedAt: Date.now() - 1000 * 60 * 60 * 24 * 2,
      paidAt: null,
      paymentId: null
    }
  ],
  notifications: []
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

export const delay = (ms = 380) => new Promise((r) => setTimeout(r, ms))
