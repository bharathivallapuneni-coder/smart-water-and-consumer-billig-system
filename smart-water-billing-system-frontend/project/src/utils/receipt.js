export function downloadReceipt(bill, resident) {
  const lines = [
    'HYDROBILL — PAYMENT RECEIPT',
    '================================',
    `Building        : ${resident.buildingName || ''}`,
    `Flat number      : ${resident.flatNumber}`,
    `Resident         : ${resident.name}`,
    '--------------------------------',
    `Billing period   : ${bill.month} ${bill.year}`,
    `Units consumed   : ${bill.usage}`,
    `Amount paid      : Rs. ${bill.amount}`,
    `Payment ID       : ${bill.paymentId}`,
    `Paid on          : ${new Date(bill.paidAt).toLocaleString('en-IN')}`,
    '--------------------------------',
    'Status: PAID',
    '',
    'This is a system-generated receipt.'
  ]
  const blob = new Blob([lines.join('\n')], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `receipt-${bill.month}-${bill.year}-${resident.flatNumber}.txt`
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}
