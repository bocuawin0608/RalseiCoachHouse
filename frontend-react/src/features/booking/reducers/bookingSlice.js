import { createSlice } from '@reduxjs/toolkit';
import { v4 as uuidv4 } from 'uuid';

const initialState = {
    step: 1,
    holdToken: uuidv4(), 
    selectedSeatIds: [],
    passengerInfo: {
        fullName: '',
        phone: '',
        email: ''
    }
};

const bookingSlice = createSlice({
    name: 'booking',
    initialState,
    reducers: {
        
        setStep: (state, action) => {
            state.step = action.payload;
        },
        
        setSelectedSeats: (state, action) => {
            state.selectedSeatIds = action.payload;
        },
        
        setPassengerInfo: (state, action) => {
            state.passengerInfo = action.payload;
        },
        
        resetBooking: () => {
            return {...initialState, holdToken: uuidv4()}
        }
    }
});

export const { setStep, setSelectedSeats, setPassengerInfo, resetBooking } = bookingSlice.actions;

export default bookingSlice.reducer;