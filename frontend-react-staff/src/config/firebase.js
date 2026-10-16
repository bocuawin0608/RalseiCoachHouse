import { initializeApp } from 'firebase/app';
import { getAuth, connectAuthEmulator } from 'firebase/auth';

const apiKey = import.meta.env.VITE_FIREBASE_API_KEY;
const authDomain = import.meta.env.VITE_FIREBASE_AUTH_DOMAIN;
const projectId = import.meta.env.VITE_FIREBASE_PROJECT_ID;
const storageBucket = import.meta.env.VITE_FIREBASE_STORAGE_BUCKET;
const messagingSenderId = import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID;
const appId = import.meta.env.VITE_FIREBASE_APP_ID;

const firebaseConfig = {
    apiKey: (apiKey && apiKey !== 'undefined') ? apiKey : 'AIzaSyC6L7I7N6Wdvzkx_wMFScubaiNDs2hePw4',
    authDomain: (authDomain && authDomain !== 'undefined') ? authDomain : 'swp-firebase-dbdba.firebaseapp.com',
    projectId: (projectId && projectId !== 'undefined') ? projectId : 'swp-firebase-dbdba',
    storageBucket: (storageBucket && storageBucket !== 'undefined') ? storageBucket : 'swp-firebase-dbdba.firebasestorage.app',
    messagingSenderId: (messagingSenderId && messagingSenderId !== 'undefined') ? messagingSenderId : '522469509916',
    appId: (appId && appId !== 'undefined') ? appId : '1:522469509916:web:9285df72bec0f97fe7c8d9',
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

if (import.meta.env.VITE_USE_FIREBASE_EMULATOR === 'true') {
    connectAuthEmulator(auth, 'http://localhost:9099');
}

export { auth };
export default app;
