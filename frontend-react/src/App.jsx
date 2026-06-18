import AppRouter from './routes/AppRouter';

function App() {
  return (
    <>
      <AppRouter />
      
      {/* Có thể vứt cái ToastContainer ở đây để bắt thông báo toàn app */}
      {/* <ToastContainer position="top-right" /> */}
    </>
  );
}

export default App;