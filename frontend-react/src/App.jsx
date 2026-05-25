import AppRouter from './routes/AppRouter';

function App() {
  return (
    <div className="app-container">
      {/* <Header /> */}

      <main style={{ minHeight: '80vh' }}>
        <AppRouter />
      </main>

      {/* <Footer /> */}
    </div>
  );
}

export default App;