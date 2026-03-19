import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import Providers from './pages/Providers'
import Models from './pages/Models'
import RoutesPage from './pages/Routes'
import UsageStats from './pages/UsageStats'
import ModelTest from './pages/ModelTest'

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/providers" element={<Providers />} />
        <Route path="/models" element={<Models />} />
        <Route path="/routes" element={<RoutesPage />} />
        <Route path="/usage" element={<UsageStats />} />
        <Route path="/test" element={<ModelTest />} />
      </Routes>
    </Layout>
  )
}

export default App
