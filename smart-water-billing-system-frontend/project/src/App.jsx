import React from 'react'
import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'

import Landing from './pages/Landing'
import Login from './pages/auth/Login'
import OwnerRegister from './pages/auth/OwnerRegister'
import ActivateAccount from './pages/auth/ActivateAccount'

import SuperAdminLayout from './pages/superadmin/SuperAdminLayout'
import Overview from './pages/superadmin/Overview'
import PendingRequests from './pages/superadmin/PendingRequests'
import ApprovedBuildings from './pages/superadmin/ApprovedBuildings'

import OwnerLayout from './pages/owner/OwnerLayout'
import OwnerDashboard from './pages/owner/OwnerDashboard'
import OwnerProfile from './pages/owner/Profile'
import TariffConfig from './pages/owner/TariffConfig'
import BulkWaterPurchases from './pages/owner/BulkWaterPurchases'
import BillingCycleManager from './pages/owner/BillingCycleManager'
import Residents from './pages/owner/Residents'
import MeterEntry from './pages/owner/MeterEntry'
import BillGeneration from './pages/owner/BillGeneration'
import PaymentStatus from './pages/owner/PaymentStatus'
import Reports from './pages/owner/Reports'

import ResidentLayout from './pages/resident/ResidentLayout'
import ResidentDashboard from './pages/resident/ResidentDashboard'
import ResidentAlerts from './pages/resident/ResidentAlerts'
import ResidentProfile from './pages/resident/Profile'
import MeterReading from './pages/resident/MeterReading'
import CurrentBill from './pages/resident/CurrentBill'
import PreviousBills from './pages/resident/PreviousBills'
import PaymentHistory from './pages/resident/PaymentHistory'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<OwnerRegister />} />
      <Route path="/resident/activate" element={<ActivateAccount />} />

      <Route
        path="/super-admin"
        element={
          <ProtectedRoute allow={['SUPER_ADMIN', 'SUPERADMIN']}>
            <SuperAdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Overview />} />
        <Route path="requests" element={<PendingRequests />} />
        <Route path="buildings" element={<ApprovedBuildings />} />
      </Route>

      <Route
        path="/owner"
        element={
          <ProtectedRoute allow={['BUILDING_OWNER']}>
            <OwnerLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<OwnerDashboard />} />
        <Route path="dashboard" element={<OwnerDashboard />} />
        <Route path="profile" element={<OwnerProfile />} />
        <Route path="tariffs" element={<TariffConfig buildingId="bld_1002" />} />
        <Route path="water-purchases" element={<BulkWaterPurchases buildingId="bld_1002" />} />
        <Route path="billing-cycles" element={<BillingCycleManager buildingId="bld_1002" />} />
        <Route path="residents" element={<Residents />} />
        <Route path="meter-entry" element={<MeterEntry />} />
        <Route path="bill-generation" element={<BillGeneration />} />
        <Route path="bills" element={<BillGeneration />} />
        <Route path="payment-status" element={<PaymentStatus />} />
        <Route path="payments" element={<PaymentStatus />} />
        <Route path="reports" element={<Reports />} />
      </Route>

      <Route
        path="/resident"
        element={
          <ProtectedRoute allow={['RESIDENT']}>
            <ResidentLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<ResidentDashboard />} />
        <Route path="alerts" element={<ResidentAlerts />} />
        <Route path="profile" element={<ResidentProfile />} />
        <Route path="meter" element={<MeterReading />} />
        <Route path="bill" element={<CurrentBill />} />
        <Route path="history" element={<PreviousBills />} />
        <Route path="payments" element={<PaymentHistory />} />
      </Route>

      <Route path="*" element={<Landing />} />
    </Routes>
  )
}
