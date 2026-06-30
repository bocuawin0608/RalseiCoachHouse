import { createSlice } from '@reduxjs/toolkit';
import { v4 as uuidv4 } from 'uuid';

const initialState = {
    step: 1,
    holdToken: uuidv4(), 
    selectedSeats: [],
    passengerInfo: {
        pickupStopId: null,
        dropoffStopId: null,
        voucherId: null,
        passengers: []
    },
    paymentInfo: null,
    tripInfo: null
};

const bookingSlice = createSlice({
    name: 'booking',
    initialState,
    reducers: {
        
        setStep: (state, action) => {
            state.step = action.payload;
        },
        
        setSelectedSeats: (state, action) => {
            state.selectedSeats = action.payload;
        },
        
        setPassengerInfo: (state, action) => {
            state.passengerInfo = action.payload;
        },

        setPaymentInfo: (state, action) => {
            state.paymentInfo = action.payload;
        },

        setPaymentStatus: (state, action) => {
            if (state.paymentInfo) {
                state.paymentInfo.status = action.payload;
            }
        },
        setTripInfo: (state, action) => { 
            state.tripInfo = action.payload;
        },
        resetBooking: () => {
            return {...initialState, holdToken: uuidv4()}
        }
    }
});

export const { setStep, setSelectedSeats, setPassengerInfo, setPaymentInfo, setPaymentStatus, setTripInfo, resetBooking } = bookingSlice.actions;

export default bookingSlice.reducer;
