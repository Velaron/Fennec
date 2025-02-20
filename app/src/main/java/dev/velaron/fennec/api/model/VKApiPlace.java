//
//  Copyright (c) 2014 VK.com
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy of
//  this software and associated documentation files (the "Software"), to deal in
//  the Software without restriction, including without limitation the rights to
//  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
//  the Software, and to permit persons to whom the Software is furnished to do so,
//  subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
//  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
//  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
//  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//

package dev.velaron.fennec.api.model;

/**
 * A place object describes a location.
 */
@SuppressWarnings("unused")
public class VKApiPlace {

    /**
     * Location ID.
     */
    public int id;

    /**
     * Location title.
     */
    public String title;

    /**
     * Geographical latitude, in degrees (from -90 to 90).
     */
    public double latitude;

    /**
     * Geographical longitude, in degrees (from -180 to 180)
     */
    public double longitude;

    /**
     * Date (in Unix time) when the location was added
     */
    public long created;

    /**
     * Numbers of checkins in this place
     */
    public int checkins;

    /**
     * Date (in Unix time) when the location was last time updated
     */
    public long updated;

    /**
     * ID of the country the place is located in, positive number
     */
    public int country_id;

    /**
     * ID of the city the place is located in, positive number
     */
    public int city_id;

    /**
     * Location address.
     */
    public String address;

    /**
     * Creates empty Place instance.
     */
    public VKApiPlace() {

    }
}