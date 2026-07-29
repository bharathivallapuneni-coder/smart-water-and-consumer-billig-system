import api from './axios'
import { loadDb, saveDb, uid, delay } from './mockDb'
import { calculateUsage, calculateBill } from '../utils/tariff'

// Flip to false once your Spring Boot endpoints below are live.
// Every function has a matching REST call commented next to it —
// that's the exact contract the backend controllers should expose.
export const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true'

function pushNotification(db, { forRole, forId, message }) {
  db.notifications.unshift({
    id: uid('ntf'),
    forRole,
    forId,
    message,
    createdAt: Date.now(),
    read: false
  })
}

// ---------------------------------------------------------------------------
// AUTH
// ---------------------------------------------------------------------------
export async function loginRequest({ role, username, password }) {
  if (!USE_MOCK) {
    // POST /api/auth/login  { role, username, password } -> { token, user }
    const { data } = await api.post('/auth/login', { role, username, password })
    return data
  }
  await delay()
  const db = loadDb()

  if (role === 'SUPER_ADMIN') {
    if (username === db.superAdmin.username && password === db.superAdmin.password) {
      return { token: 'mock.superadmin.token', user: { role, username, name: 'Super Admin' } }
    }
    throw new Error('Invalid super admin credentials')
  }

  if (role === 'BUILDING_OWNER') {
    const b = db.buildings.find((x) => x.username === username && x.password === password)
    if (!b) throw new Error('Invalid username or password')
    if (b.status === 'PENDING') throw new Error('Your registration is still awaiting Super Admin approval')
    if (b.status === 'REJECTED') throw new Error('Your registration request was rejected')
    return { token: `mock.owner.${b.id}`, user: { role, id: b.id, name: b.ownerName, buildingName: b.buildingName } }
  }

  if (role === 'RESIDENT') {
    const r = db.residents.find((x) => x.username === username && x.password === password)
    if (!r) throw new Error('Invalid username or password')
    const building = db.buildings.find((b) => b.id === r.buildingId)
    return {
      token: `mock.resident.${r.id}`,
      user: { role, id: r.id, name: r.name, flatNumber: r.flatNumber, buildingId: r.buildingId, buildingName: building?.buildingName }
    }
  }
  throw new Error('Unknown role')
}

export async function registerBuildingOwner(payload) {
  if (!USE_MOCK) {
    // POST /api/owners/register  (multipart or json) -> 201 Created
    const { data } = await api.post('/owners/register', payload)
    return data
  }
  await delay()
  const db = loadDb()
  const exists = db.buildings.some((b) => b.username === payload.username)
  if (exists) throw new Error('Username already taken, choose another')
  const record = {
    id: uid('bld'),
    ...payload,
    status: 'PENDING',
    createdAt: Date.now()
  }
  db.buildings.unshift(record)
  saveDb(db)
  return record
}

// ---------------------------------------------------------------------------
// SUPER ADMIN
// ---------------------------------------------------------------------------
export async function fetchBuildings() {
  if (!USE_MOCK) {
    // GET /api/admin/buildings
    const { data } = await api.get('/admin/buildings')
    return data
  }
  await delay()
  return loadDb().buildings
}

export async function decideBuildingRequest(buildingId, decision /* 'APPROVED' | 'REJECTED' */) {
  if (!USE_MOCK) {
    // PATCH /api/admin/buildings/{id}/status  { status: decision }
    const { data } = await api.patch(`/admin/buildings/${buildingId}/status`, { status: decision })
    return data
  }
  await delay()
  const db = loadDb()
  const b = db.buildings.find((x) => x.id === buildingId)
  if (!b) throw new Error('Building not found')
  b.status = decision
  if (decision === 'APPROVED') {
    pushNotification(db, { forRole: 'BUILDING_OWNER', forId: b.id, message: `${b.buildingName} was approved. You can now log in.` })
  }
  saveDb(db)
  return b
}

export async function fetchAdminStats() {
  if (!USE_MOCK) {
    const { data } = await api.get('/admin/stats')
    return data
  }
  await delay()
  const db = loadDb()
  return {
    pending: db.buildings.filter((b) => b.status === 'PENDING').length,
    approved: db.buildings.filter((b) => b.status === 'APPROVED').length,
    totalBuildings: db.buildings.length,
    totalResidents: db.residents.length
  }
}

// ---------------------------------------------------------------------------
// BUILDING OWNER
// ---------------------------------------------------------------------------
export async function fetchResidents(buildingId) {
  if (!USE_MOCK) {
    // GET /api/owners/{buildingId}/residents
    const { data } = await api.get(`/owners/${buildingId}/residents`)
    return data
  }
  await delay()
  return loadDb().residents.filter((r) => r.buildingId === buildingId)
}

export async function createResident(buildingId, payload) {
  if (!USE_MOCK) {
    // POST /api/owners/{buildingId}/residents
    const { data } = await api.post(`/owners/${buildingId}/residents`, payload)
    return data
  }
  await delay()
  const db = loadDb()
  const record = {
    id: uid('res'),
    buildingId,
    invitationStatus: 'PENDING',
    createdAt: Date.now(),
    ...payload
  }
  db.residents.unshift(record)
  pushNotification(db, { forRole: 'RESIDENT', forId: record.id, message: `Welcome ${record.name}! Your flat ${record.flatNumber} account was created.` })
  saveDb(db)
  return record
}

export async function updateResident(buildingId, residentId, payload) {
  if (!USE_MOCK) {
    // PATCH /api/owners/{buildingId}/residents/{residentId}
    const { data } = await api.patch(`/owners/${buildingId}/residents/${residentId}`, payload)
    return data
  }
  await delay()
  const db = loadDb()
  const resident = db.residents.find((r) => r.id === residentId && r.buildingId === buildingId)
  if (!resident) throw new Error('Resident not found')
  Object.assign(resident, payload)
  saveDb(db)
  return resident
}

export async function deleteResident(buildingId, residentId) {
  if (!USE_MOCK) {
    // DELETE /api/owners/{buildingId}/residents/{residentId}
    const { data } = await api.delete(`/owners/${buildingId}/residents/${residentId}`)
    return data
  }
  await delay()
  const db = loadDb()
  const index = db.residents.findIndex((r) => r.id === residentId && r.buildingId === buildingId)
  if (index === -1) throw new Error('Resident not found')
  const [removed] = db.residents.splice(index, 1)
  saveDb(db)
  return removed
}

export async function submitMeterReading(buildingId, payload) {
  if (!USE_MOCK) {
    // POST /api/owners/{buildingId}/meter-readings
    const { data } = await api.post(`/owners/${buildingId}/meter-readings`, payload)
    return data
  }
  await delay()
  const db = loadDb()
  const usage = calculateUsage(payload.previousReading, payload.currentReading)
  const record = {
    id: uid('mtr'),
    buildingId,
    usage,
    createdAt: Date.now(),
    ...payload
  }
  db.meterReadings.unshift(record)
  saveDb(db)
  return record
}

export async function fetchMeterReadings(buildingId) {
  if (!USE_MOCK) {
    const { data } = await api.get(`/owners/${buildingId}/meter-readings`)
    return data
  }
  await delay()
  return loadDb().meterReadings.filter((m) => m.buildingId === buildingId)
}

export async function generateBill(buildingId, meterReadingId) {
  if (!USE_MOCK) {
    // POST /api/owners/{buildingId}/bills  { meterReadingId }
    const { data } = await api.post(`/owners/${buildingId}/bills`, { meterReadingId })
    return data
  }
  await delay()
  const db = loadDb()
  const reading = db.meterReadings.find((m) => m.id === meterReadingId)
  if (!reading) throw new Error('Meter reading not found')
  const amount = calculateBill(reading.usage)
  const record = {
    id: uid('bill'),
    buildingId,
    residentId: reading.residentId,
    meterReadingId,
    month: reading.month,
    year: reading.year,
    usage: reading.usage,
    amount,
    status: 'PENDING',
    generatedAt: Date.now(),
    paidAt: null,
    paymentId: null
  }
  db.bills.unshift(record)
  pushNotification(db, { forRole: 'RESIDENT', forId: reading.residentId, message: `Your ${reading.month} bill of ₹${amount} is ready.` })
  saveDb(db)
  return record
}

export async function fetchBillsForBuilding(buildingId) {
  if (!USE_MOCK) {
    const { data } = await api.get(`/owners/${buildingId}/bills`)
    return data
  }
  await delay()
  return loadDb().bills.filter((b) => b.buildingId === buildingId)
}

// ---------------------------------------------------------------------------
// RESIDENT
// ---------------------------------------------------------------------------
export async function fetchResidentBills(residentId) {
  if (!USE_MOCK) {
    // GET /api/residents/{id}/bills
    const { data } = await api.get(`/residents/${residentId}/bills`)
    return data
  }
  await delay()
  return loadDb()
    .bills.filter((b) => b.residentId === residentId)
    .sort((a, b) => b.generatedAt - a.generatedAt)
}

export async function fetchResidentReadings(residentId) {
  if (!USE_MOCK) {
    const { data } = await api.get(`/residents/${residentId}/meter-readings`)
    return data
  }
  await delay()
  return loadDb()
    .meterReadings.filter((m) => m.residentId === residentId)
    .sort((a, b) => b.createdAt - a.createdAt)
}

export async function markBillPaid(billId, paymentRef) {
  if (!USE_MOCK) {
    // POST /api/payments/confirm  { billId, paymentRef }
    const { data } = await api.post('/payments/confirm', { billId, paymentRef })
    return data
  }
  await delay()
  const db = loadDb()
  const bill = db.bills.find((b) => b.id === billId)
  if (!bill) throw new Error('Bill not found')
  bill.status = 'PAID'
  bill.paidAt = Date.now()
  bill.paymentId = paymentRef
  saveDb(db)
  return bill
}
