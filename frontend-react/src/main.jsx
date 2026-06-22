import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { Provider as ReduxProvider } from 'react-redux';
import { store } from './stores/store.js';
import { AuthProvider } from './features/auth';
// import { NotificationProvider } from './contexts/NotificationContext';
import App from './App.jsx';

import 'bootstrap/dist/css/bootstrap.min.css'
import './assets/styles/global.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ReduxProvider store={store}>
      <BrowserRouter>
        {/* <NotificationProvider> */}
          <AuthProvider>
            <App />
          </AuthProvider>
        {/* </NotificationProvider> */}
      </BrowserRouter>
    </ReduxProvider>
  </StrictMode>
);