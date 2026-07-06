export const transformFormToPassengerPayload = (formPassengers, verifiedPhones = {}) =>
    formPassengers.map((passenger) => {
        const phone = passenger.phone?.trim();
        const verification = phone ? verifiedPhones[phone] : null;

        return {
            tripSeatId: Number(passenger.tripSeatId),
            fullname: passenger.fullname?.trim(),
            phone,
            email: passenger.email?.trim() || null,
            firebaseIdToken: verification?.isKnown ? null : (verification?.idToken ?? null),
            accompaniedChild: passenger.hasChild && passenger.childName
                ? {
                    fullname: passenger.childName.trim(),
                    birthYear: Number(passenger.childBirthYear),
                }
                : null,
        };
    });