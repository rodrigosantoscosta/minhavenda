import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import NotFound from './pages/NotFound'
import TestAPI from './pages/TestAPI'


function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        {/* Header vai aqui depois */}
        
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="*" element={<NotFound />} />
          <Route path="/test-api" element={<TestAPI />} />
        </Routes>

        {/* Footer vai aqui depois */}
      </div>
    </BrowserRouter>
  )
}

export default App