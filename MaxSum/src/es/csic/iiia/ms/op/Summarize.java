/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2014 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.ms.op;

/**
 * Available summarization modes.
 */
public enum Summarize {
    /**
     * Perform the summzarize operator using maximum.
     */
    MAX {
        @Override
        public double eval(double x, double y) {
            return Math.max(x, y);
        }

        @Override
        public boolean isBetter(double x, double y) {
            return x - y > 0.00001;
        }

        @Override
        public double getNoGood() {
            return Double.NEGATIVE_INFINITY;
        }
    },

    /**
     * Perform the summzarize operator using minimum.
     */
    MIN {
        @Override
        public double eval(double x, double y) {
            return Math.min(x, y);
        }

        @Override
        public boolean isBetter(double x, double y) {
            return y - x > 0.00001;
        }

        @Override
        public double getNoGood() {
            return Double.POSITIVE_INFINITY;
        }
    },

    /**
     * Perform the summarize operator using addition.
     */
    SUM {
        @Override
        public double eval(double x, double y) {
            return x + y;
        }

        @Override
        public boolean isBetter(double x, double y) {
            throw new RuntimeException("I don't know how to compare when using SUM summarization.");
        }

        @Override
        public double getNoGood() {
            return 0;
        }
    };

    /**
     * Performs the summarization of the given values according to the current summarization
     * mode.
     *
     * @param x first value.
     * @param y second value.
     * @return summarization result.
     */
    public abstract double eval(double x, double y);

    /**
     * Returns <em>true</em> if x is better than y according to the summarization mode being
     * used.
     *
     * @param x first value.
     * @param y second value.
     * @return true if x is better than y or <em>false</em> otherwise.
     */
    public abstract boolean isBetter(double x, double y);

    /**
     * Returns the value corresponding to a <em>nogood</em> (infitely bad value) according to
     * the summarization mode being used.
     *
     * @return <em>nogood</em> (worst) value.
     */
    public abstract double getNoGood();

}
