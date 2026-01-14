import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { ToastProvider } from './components/common/Toast'
import Header from './components/layout/Header'
import Footer from './components/layout/Footer'

// Páginas (você vai criar estas depois)
import Home from './pages/Home'
import Login from './pages/Login'
import NotFound from './pages/NotFound'
import TestComponents from './pages/TestComponents'  // Página de teste
// import ProductPage from './pages/ProductPage'

function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-gray-50 flex flex-col">
          {/* Header em todas as páginas */}
          <Header />
          
          {/* Conteúdo principal */}
          <main className="flex-1">
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/test" element={<TestComponents />} />
              <Route path="*" element={<NotFound />} />
              {/* <Route path='/product' element={<ProductPage />} /> */}

            </Routes>
          </main>

          {/* Footer em todas as páginas */}
          <Footer />
        </div>
      </BrowserRouter>
    </ToastProvider>
  )
}

export default App