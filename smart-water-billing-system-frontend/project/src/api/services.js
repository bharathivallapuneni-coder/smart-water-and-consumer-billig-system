import api from './axios'
import { loadDb, saveDb, uid, delay } from './mockDb'
import { calculateUsage, calculateBill } from '../utils/tariff'

// Flip to false once your Spring Boot endpoints below are live.
export const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true'

function pushNotification(db, { forRole, forId, title, message, notificationType = 'INFO' }) {
  db.notifications.unshift({
    id: uid('ntf'),
    forRole,
    forId,
    title: title || 'Notification',
    message,
    notificationType,
    createdAt: Date.now(),
    read: false
  })
}

// ---------------------------------------------------------------------------
// AUTH & PASSWORD MANAGEMENT
// ---------------------------------------------------------------------------
export async function loginRequest({ role, username, password }) {
  if (!USE_MOCK) {
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
  if (payload.password !== payload.confirmPassword) {
    throw new Error('Password and Confirm Password do not match')
  }
  if (!USE_MOCK) {
    const { data } = await api.post('/auth/register', payload)
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
  delete record.confirmPassword
  db.buildings.unshift(record)
  saveDb(db)
  return record
}

export async function requestPasswordReset(email) {
  if (!USE_MOCK) {
    const { data } = await api.post('/auth/forgot-password', { email })
    return data
  }
  await delay()
  const db = loadDb()
  const tokenCode = String(Math.floor(100000 + Math.random() * 900000))
  db.resetTokens.unshift({
    email,
    token: tokenCode,
    expiry: Date.now() + 15 * 60 * 1000,
    used: false
  })
  saveDb(db)
  return `If an account with ${email} exists, password reset OTP ${tokenCode} has been generated.`
}

export async function resetPassword({ token, newPassword, confirmPassword }) {
  if (newPassword !== confirmPassword) {
    throw new Error('New password and confirm password do not match')
  }
  if (!USE_MOCK) {
    const { data } = await api.post('/auth/reset-password', { token, newPassword, confirmPassword })
    return data
  }
  await delay()
  const db = loadDb()
  const t = db.resetTokens.find((x) => x.token === token && !x.used && x.expiry > Date.now())
  if (!t) throw new Error('Invalid or expired reset token/OTP')

  // Update password in matching role entity
  const owner = db.buildings.find((b) => b.email === t.email)
  if (owner) owner.password = newPassword

  const resident = db.residents.find((r) => r.email === t.email)
  if (resident) resident.password = newPassword

  if (db.superAdmin.email === t.email) db.superAdmin.password = newPassword

  t.used = true
  saveDb(db)
  return 'Password reset successfully. Please log in with your new password.'
}

export async function changePassword({ userId, currentPassword, newPassword, confirmPassword }) {
  if (newPassword !== confirmPassword) {
    throw new Error('New password and confirm password do not match')
  }
  if (!USE_MOCK) {
    const { data } = await api.post('/auth/change-password', { currentPassword, newPassword, confirmPassword })
    return data
  }
  await delay()
  const db = loadDb()

  const owner = db.buildings.find((b) => b.id === userId)
  if (owner) {
    if (owner.password !== currentPassword) throw new Error('Invalid current password')
    owner.password = newPassword
    saveDb(db)
    return 'Password updated successfully'
  }

  const resident = db.residents.find((r) => r.id === userId)
  if (resident) {
    if (resident.password !== currentPassword) throw new Error('Invalid current password')
    resident.password = newPassword
    saveDb(db)
    return 'Password updated successfully'
  }

  throw new Error('User not found')
}

// ---------------------------------------------------------------------------
// TIERED TARIFF CONFIGURATION
// ---------------------------------------------------------------------------
export async function fetchBuildingTariff(buildingId) {
  if (!USE_MOCK) {
    const { data } = await api.get(`/tariffs/building/${buildingId}`)
    return data
  }
  await delay()
  const db = loadDb()
  const t = db.tariffs.find((x) => x.buildingId === buildingId)
  return t ? t.tiers : [
    { tierName: 'Base Tier (0-10 kL)', minKl: 0, maxKl: 10, ratePerKl: 10, fixedCharge: 0 },
    { tierName: 'High Tier (>10 kL)', minKl: 10, maxKl: null, ratePerKl: 15, fixedCharge: 0 }
  ]
}

export async function saveBuildingTariff(buildingId, tiers) {
  if (!USE_MOCK) {
    const { data } = await api.post(`/tariffs/building/${buildingId}`, tiers)
    return data
  }
  await delay()
  const db = loadDb()
  let t = db.tariffs.find((x) => x.buildingId === buildingId)
  if (!t) {
    t = { id: uid('trf'), buildingId, tiers }
    db.tariffs.push(t)
  } else {
    t.tiers = tiers
  }
  saveDb(db)
  return t.tiers
}

// ---------------------------------------------------------------------------
// BULK WATER PURCHASES
// ---------------------------------------------------------------------------
export async function fetchBulkPurchases(buildingId) {
  if (!USE_MOCK) {
    const { data } = await api.get(`/bulk-purchases/building/${buildingId}`)
    return data
  }
  await delay()
  return loadDb().bulkPurchases.filter((p) => p.buildingId === buildingId)
}

export async function createBulkPurchase(payload) {
  if (!USE_MOCK) {
    const { data } = await api.post('/bulk-purchases', payload)
    return data
  }
  await delay()
  const db = loadDb()
  const volume = Number(payload.purchasedVolumeKl)
  const cost = Number(payload.totalCost)
  const unitCost = volume > 0 ? cost / volume : 0
  const record = {
    id: uid('pur'),
    unitCostPerKl: Math.round(unitCost * 100) / 100,
    ...payload,
    createdAt: Date.now()
  }
  db.bulkPurchases.unshift(record)
  saveDb(db)
  return record
}

export async function updateBulkPurchase(id, payload) {
  if (!USE_MOCK) {
    const { data } = await api.put(`/bulk-purchases/${id}`, payload)
    return data
  }
  await delay()
  const db = loadDb()
  const p = db.bulkPurchases.find((x) => x.id === id)
  if (!p) throw new Error('Purchase record not found')
  Object.assign(p, payload)
  if (p.purchasedVolumeKl > 0) {
    p.unitCostPerKl = Math.round((p.totalCost / p.purchasedVolumeKl) * 100) / 100
  }
  saveDb(db)
  return p
}

export async function deleteBulkPurchase(id) {
  if (!USE_MOCK) {
    const { data } = await api.delete(`/bulk-purchases/${id}`)
    return data
  }
  await delay()
  const db = loadDb()
  const idx = db.bulkPurchases.findIndex((x) => x.id === id)
  if (idx !== -1) db.bulkPurchases.splice(idx, 1)
  saveDb(db)
  return true
}

// ---------------------------------------------------------------------------
// BILLING CYCLE & COST DISTRIBUTION ENGINE
// ---------------------------------------------------------------------------
export async function fetchBuildingCycles(buildingId) {
  if (!USE_MOCK) {
    const { data } = await api.get(`/billing-cycles/household/${buildingId}`)
    return data
  }
  await delay()
  return loadDb().billingCycles.filter((c) => c.buildingId === buildingId)
}

export async function openBillingCycle(buildingId, { month, year }) {
  if (!USE_MOCK) {
    const { data } = await api.post('/billing-cycles/generate', { apartmentId: buildingId, billingMonth: month, billingYear: year })
    return data
  }
  await delay()
  const db = loadDb()
  const exists = db.billingCycles.some((c) => c.buildingId === buildingId && c.month === month && c.year === year)
  if (exists) throw new Error(`Billing cycle for ${month} ${year} already exists`)

  const cycle = {
    id: uid('bcy'),
    buildingId,
    month,
    year,
    status: 'OPEN',
    openedAt: Date.now(),
    finalizedAt: null
  }
  db.billingCycles.unshift(cycle)
  saveDb(db)
  return cycle
}

export async function finalizeBillingCycle(buildingId, cycleId) {
  if (!USE_MOCK) {
    const { data } = await api.post(`/billing-cycles/${cycleId}/finalize`)
    return data
  }
  await delay()
  const db = loadDb()
  const cycle = db.billingCycles.find((c) => c.id === cycleId && c.buildingId === buildingId)
  if (!cycle) throw new Error('Billing cycle not found')
  if (cycle.status === 'FINALIZED') throw new Error('Billing cycle is already finalized')

  const residents = db.residents.filter((r) => r.buildingId === buildingId)
  const purchases = db.bulkPurchases.filter((p) => p.buildingId === buildingId)
  const totalProcurementCost = purchases.reduce((acc, p) => acc + Number(p.totalCost), 0)

  // Tariff calculation
  const tariffObj = db.tariffs.find((t) => t.buildingId === buildingId)
  const tiers = tariffObj ? tariffObj.tiers : [
    { minKl: 0, maxKl: 10, ratePerKl: 10 },
    { minKl: 10, maxKl: null, ratePerKl: 15 }
  ]

  // Metred vs Unmetered distribution setup
  const meteredResidents = residents.filter((r) => r.isMetered !== false)
  const unmeteredResidents = residents.filter((r) => r.isMetered === false)

  let totalMeteredUsage = 0
  const residentUsages = {}

  residents.forEach((r) => {
    const reading = db.meterReadings.find((m) => m.residentId === r.id && m.month === cycle.month && m.year === cycle.year)
    const usage = reading ? Number(reading.usage) : 0
    residentUsages[r.id] = usage
    if (r.isMetered !== false) totalMeteredUsage += usage
  })

  const totalUnmeteredArea = unmeteredResidents.reduce((sum, r) => sum + (r.flatArea || 1000), 0)

  // Generate Invoices for each household
  residents.forEach((r) => {
    const usage = residentUsages[r.id]

    // Tiered base charge calculation
    let baseCharge = 0
    let u = usage
    if (u <= 10) {
      baseCharge = u * 10
    } else {
      baseCharge = (10 * 10) + ((u - 10) * 15)
    }

    // Procurement cost distribution
    let procurementCharge = 0
    if (r.isMetered !== false) {
      if (totalMeteredUsage > 0) {
        procurementCharge = Math.round((totalProcurementCost * (usage / totalMeteredUsage)) * 100) / 100
      }
    } else {
      if (totalUnmeteredArea > 0) {
        const area = r.flatArea || 1000
        procurementCharge = Math.round((totalProcurementCost * (area / totalUnmeteredArea)) * 100) / 100
      }
    }

    // Shared area charge
    const sharedAreaCharge = 150

    const totalAmount = baseCharge + procurementCharge + sharedAreaCharge
    const invNumber = `INV-${cycle.year}${cycle.month.slice(0, 3).toUpperCase()}-${r.flatNumber}`

    const invoice = {
      id: uid('inv'),
      invoiceNumber: invNumber,
      billingCycleId: cycle.id,
      residentId: r.id,
      buildingId,
      flatNumber: r.flatNumber,
      residentName: r.name,
      billingPeriod: `${cycle.month} ${cycle.year}`,
      meteredConsumptionKl: usage,
      flatAreaSqft: r.flatArea || 1000,
      isMetered: r.isMetered !== false,
      baseTieredCharge: baseCharge,
      allocatedWaterProcurementCharge: procurementCharge,
      sharedAreaCharge,
      adjustments: 0,
      totalAmount,
      status: 'PENDING',
      generatedAt: Date.now(),
      dueDate: '2026-08-25',
      paidAt: null,
      paymentId: null
    }

    db.invoices.unshift(invoice)

    // Notify resident
    pushNotification(db, {
      forRole: 'RESIDENT',
      forId: r.id,
      title: 'New Itemized Invoice Generated',
      message: `Your water bill for ${cycle.month} ${cycle.year} is ₹${totalAmount}. Due by 25th.`,
      notificationType: 'INVOICE_GENERATED'
    })
  })

  cycle.status = 'FINALIZED'
  cycle.finalizedAt = Date.now()
  saveDb(db)
  return cycle
}

// ---------------------------------------------------------------------------
// INVOICES & NOTIFICATIONS
// ---------------------------------------------------------------------------
export async function fetchResidentInvoices(residentId) {
  if (!USE_MOCK) {
    const { data } = await api.get('/invoices/resident')
    return data
  }
  await delay()
  return loadDb().invoices.filter((i) => i.residentId === residentId)
}

export async function payInvoice(invoiceId, paymentRef) {
  if (!USE_MOCK) {
    const { data } = await api.post(`/invoices/${invoiceId}/pay`, { paymentRef })
    return data
  }
  await delay()
  const db = loadDb()
  const inv = db.invoices.find((i) => i.id === invoiceId)
  if (!inv) throw new Error('Invoice not found')
  inv.status = 'PAID'
  inv.paidAt = Date.now()
  inv.paymentId = paymentRef || 'pay_mock_' + Math.floor(Math.random() * 10000)
  saveDb(db)
  return inv
}

export async function fetchNotifications(forRole, forId) {
  if (!USE_MOCK) {
    const { data } = await api.get('/notifications')
    return data
  }
  await delay()
  return loadDb().notifications.filter((n) => n.forRole === forRole && n.forId === forId)
}

export async function markNotificationAsRead(id) {
  if (!USE_MOCK) {
    const { data } = await api.patch(`/notifications/${id}/read`)
    return data
  }
  await delay()
  const db = loadDb()
  const n = db.notifications.find((x) => x.id === id)
  if (n) n.read = true
  saveDb(db)
  return true
}

// Keep existing exports for backward compatibility
export async function fetchBuildings() { return loadDb().buildings }
export async function decideBuildingRequest(id, decision) {
  const db = loadDb()
  const b = db.buildings.find((x) => x.id === id)
  if (b) { b.status = decision; saveDb(db) }
  return b
}
export async function fetchAdminStats() {
  const db = loadDb()
  return { pending: db.buildings.filter(b => b.status === 'PENDING').length, approved: db.buildings.filter(b => b.status === 'APPROVED').length, totalBuildings: db.buildings.length, totalResidents: db.residents.length }
}
export async function fetchResidents(buildingId) { return loadDb().residents.filter(r => r.buildingId === buildingId) }
export async function createResident(buildingId, payload) {
  const db = loadDb()
  const record = { id: uid('res'), buildingId, invitationStatus: 'ACCEPTED', createdAt: Date.now(), flatArea: 1000, isMetered: true, ...payload }
  db.residents.unshift(record)
  saveDb(db)
  return record
}
export async function updateResident(buildingId, residentId, payload) {
  const db = loadDb()
  const r = db.residents.find((x) => x.id === residentId)
  if (r) { Object.assign(r, payload); saveDb(db) }
  return r
}
export async function deleteResident(buildingId, residentId) {
  const db = loadDb()
  const idx = db.residents.findIndex((x) => x.id === residentId)
  if (idx !== -1) db.residents.splice(idx, 1)
  saveDb(db)
}
export async function submitMeterReading(buildingId, payload) {
  const db = loadDb()
  const usage = calculateUsage(payload.previousReading, payload.currentReading)
  const record = { id: uid('mtr'), buildingId, usage, createdAt: Date.now(), ...payload }
  db.meterReadings.unshift(record)
  saveDb(db)
  return record
}
export async function generateBill(buildingId, meterReadingId) {
  const db = loadDb()
  const reading = db.meterReadings.find((m) => m.id === meterReadingId)
  const amount = calculateBill(reading ? reading.usage : 20)
  const record = { id: uid('bill'), buildingId, residentId: reading?.residentId, meterReadingId, month: reading?.month || 'July', year: reading?.year || 2026, usage: reading?.usage || 20, amount, status: 'PENDING', generatedAt: Date.now() }
  db.bills.unshift(record)
  saveDb(db)
  return record
}
export async function markBillPaid(billId, paymentRef) {
  return payInvoice(billId, paymentRef)
}
export async function fetchMeterReadings(buildingId) { return loadDb().meterReadings.filter(m => m.buildingId === buildingId) }
export async function fetchBillsForBuilding(buildingId) { return loadDb().invoices.filter(i => i.buildingId === buildingId) }
export async function fetchResidentReadings(residentId) { return loadDb().meterReadings.filter(m => m.residentId === residentId) }
export async function fetchResidentBills(residentId) { return fetchResidentInvoices(residentId) }
