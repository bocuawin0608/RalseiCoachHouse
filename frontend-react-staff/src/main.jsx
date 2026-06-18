import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './features/auth';

import App from './App.jsx'

import 'bootstrap/dist/css/bootstrap.min.css'
import './assets/styles/global.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
          <AuthProvider>
            <App />
          </AuthProvider>
      </BrowserRouter>
  </StrictMode>,
)
