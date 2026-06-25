import { configureStore } from "@reduxjs/toolkit";
import { bookingReducer } from "../features/booking";

export const store = configureStore({
    reducer: {
        booking: bookingReducer
    } 
})