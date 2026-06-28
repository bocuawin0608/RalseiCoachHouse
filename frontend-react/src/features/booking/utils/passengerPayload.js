export const transformFormToPassengerPayload = (formPassengers) => 
    formPassengers.map((passenger) => ({
        tripSeatId: Number(passenger.tripSeatId),
        fullname: passenger.fullname?.trim(),
        phone: passenger.phone?.trim(),
        email: passenger.email?.trim() || null, 
        accompaniedChild: passenger.hasChild && passenger.childName
            ? {
                fullname: passenger.childName.trim(),
                birthYear: Number(passenger.childBirthYear),
            }
            : null,
    }));