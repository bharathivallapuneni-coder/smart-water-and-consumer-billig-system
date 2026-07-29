// Mirrors the tariff slabs that should also be enforced server-side
// (Spring Boot BillingService) so the UI can preview totals instantly
// before the authoritative bill is generated on the backend.

export const TARIFF_SLABS = [
  { min: 0, max: 0, rate: 0, label: '0 units' },
  { min: 1, max: 5, rate: 100, label: '1 - 5 units' },
  { min: 6, max: 10, rate: 200, label: '6 - 10 units' },
  { min: 11, max: 20, rate: 500, label: '11 - 20 units' },
  { min: 21, max: 30, rate: 900, label: '21 - 30 units' },
  { min: 31, max: Infinity, rate: 1500, label: '31+ units' }
]

export function calculateUsage(previousReading, currentReading) {
  const usage = Number(currentReading) - Number(previousReading)
  return usage > 0 ? usage : 0
}

export function calculateBill(usage) {
  const slab = TARIFF_SLABS.find((s) => usage >= s.min && usage <= s.max)
  return slab ? slab.rate : TARIFF_SLABS[TARIFF_SLABS.length - 1].rate
}

export function slabFor(usage) {
  return TARIFF_SLABS.find((s) => usage >= s.min && usage <= s.max) || TARIFF_SLABS[0]
}

// Returns 0-100 fill percentage for the "water level" tier visualization
export function tierFillPercent(usage) {
  const maxUsage = 30
  return Math.min(100, Math.round((usage / maxUsage) * 100))
}
