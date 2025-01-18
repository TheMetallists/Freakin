/**
* Second order parametric/peaking boost filter with constant-Q
* Dimitris Tassopoulos 2016-2020
*
* fc , center frequency
* Q quality factor
* Gain/attenuation in dB
* Parametric EQs allow you to adjust the center frequency, Q and boost or cut creating any
* arbitrary bumps or notches in the frequency response. The parametric EQ is a
* variation on the ordinary band-pass and band-stop fi lters that generates symmetrical boost/
* cut curves and mixes in the dry signal to create the fi nal response. A true digital parametric
* EQ not only has independent controls, but each control only varies one coeffi cient in the
* fi lter. The parametric EQs in this section afford the same frequency response but adjustments
* in any parameter require a recalculation of all the coeffi cients. These fi lters are also called
* peaking fi lters.
*
* This design creates an almost perfect constant-Q filter with only a small amount of error
* for low-boost (or cut) values.
*/
#pragma once
#include "filter_common.h"

class SO_PARAMETRIC_CQ_BOOST : public Biquad {
public:
    tp_coeffs& calculate_coeffs(float gain_db, float Q, int fc, int fs)
    {
        coef_size_t K = 2.0 * pi * fc / fs;
        coef_size_t V0 = pow(10.0, gain_db / 20.0);
        coef_size_t d0 = 1.0 + K/Q + pow(K, 2.0);
        coef_size_t a = 1.0 + (V0*K)/Q + pow(K, 2.0);
        coef_size_t b = 2.0*(pow(K, 2.0) - 1.0);
        coef_size_t g = 1.0 - (V0*K)/Q + pow(K, 2.0);
        coef_size_t d = 1.0 - K/Q + pow(K, 2.0);
        m_coeffs.a0 = a/d0;
        m_coeffs.a1 = b/d0;
        m_coeffs.a2 = g/d0;
        m_coeffs.b1 = b/d0;
        m_coeffs.b2 = d/d0;
        m_coeffs.c0 = 1.0;
        m_coeffs.d0 = 0.0;
        return(std::ref(m_coeffs));
    }
};
