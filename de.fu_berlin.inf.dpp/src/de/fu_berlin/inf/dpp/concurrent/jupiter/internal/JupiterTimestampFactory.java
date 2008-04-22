/*
 * $Id: JupiterTimestampFactory.java 749 2005-10-21 13:51:56Z sim $
 *
 * ace - a collaborative editor
 * Copyright (C) 2005 Mark Bigler, Simon Raess, Lukas Zbinden
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TimestampFactory;

/**
 * TimestampFactory that creates Jupiter specific Timestamp objects. The
 * encoding for Jupiter specific Timestamps is a component array of length 2
 * whereby the first index of the array contains the local operation count
 * and the second index of the array contains the remote operation count.
 */
public class JupiterTimestampFactory implements TimestampFactory {

	/**
	 * @see ch.iserver.ace.algorithm.TimestampFactory#createTimestamp(int[])
	 */
	public Timestamp createTimestamp(int[] components) {
		if (components.length != 2) {
			throw new IllegalArgumentException(
					"JupiterTimestampFactory expects a component array"
					+ "of length 2");
		}
		return new JupiterVectorTime(components[0], components[1]);
	}

}
