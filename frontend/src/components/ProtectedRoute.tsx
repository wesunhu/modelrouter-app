/**
 * Route wrapper redirecting unauthenticated users to login.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { username, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div style={{ padding: 48, textAlign: 'center' }}>
        Loading...
      </div>
    )
  }

  if (username) {
    return <>{children}</>
  }

  return <Navigate to="/login" state={{ from: location }} replace />
}
