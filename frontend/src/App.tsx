/**
 * Top-level routes and auth provider wiring.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Providers from './pages/Providers'
import Models from './pages/Models'
import RoutesPage from './pages/Routes'
import UsageStats from './pages/UsageStats'
import ModelTest from './pages/ModelTest'

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={
          <ProtectedRoute>
            <Layout>
              <Dashboard />
            </Layout>
          </ProtectedRoute>
        } />
        <Route path="/providers" element={
          <ProtectedRoute>
            <Layout>
              <Providers />
            </Layout>
          </ProtectedRoute>
        } />
        <Route path="/models" element={
          <ProtectedRoute>
            <Layout>
              <Models />
            </Layout>
          </ProtectedRoute>
        } />
        <Route path="/routes" element={
          <ProtectedRoute>
            <Layout>
              <RoutesPage />
            </Layout>
          </ProtectedRoute>
        } />
        <Route path="/usage" element={
          <ProtectedRoute>
            <Layout>
              <UsageStats />
            </Layout>
          </ProtectedRoute>
        } />
        <Route path="/test" element={
          <ProtectedRoute>
            <Layout>
              <ModelTest />
            </Layout>
          </ProtectedRoute>
        } />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}

export default App
