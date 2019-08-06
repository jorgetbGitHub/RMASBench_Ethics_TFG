/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
 * All rights reserved.
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

package es.csic.iiia.ms;

import java.util.Objects;

/**
 * Immutable discrete variable, represented by an id and it's domain (number
 * of possible states).
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public final class Variable implements Comparable<Variable> {

    private final Identity id;

    private final int domain;

    /**
     * Constructs a new discrete variable.
     *
     * @param name name identifying this variable.
     * @param domain number of possible states.
     */
    public Variable(Identity id, int domain) {
        this.domain = domain;
        this.id = id;
    }

    /**
     * @return variable's identifier.
     */
    public Identity getId() {
        return id;
    }

    /**
     * @return number of possible states.
     */
    public int getDomain() {
        return domain;
    }

    /**
     * Indicates whether some other variable is "equal to" this one.
     *
     * Variables are considered "equal" if, and only if, both variables have
     * the same identifier and domain (number of possible states).
     *
     * @param obj the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Variable other = (Variable) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.domain != other.domain) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
        hash = 59 * hash + this.domain;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("V(");
        buf.append(id);
        buf.append(",");
        buf.append(domain);
        buf.append(")");

        return buf.toString();
    }

    @Override
    public int compareTo(Variable o) {
        final int cmp = id.compareTo(o.id);
        return (cmp == 0) ? Integer.compare(domain, o.domain): cmp;
    }

}
