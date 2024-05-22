const axios = require('axios');

// Function to post data
async function postData(url, data, token) {
    try {
        const response = await axios.post(url + "/api/v1/posts", data, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });
        console.log('Data posted successfully:', response.data);
    } catch (error) {
        console.error('Error posting data:', error.response ? error.response.data : error.message);
    }
}

// Function to get category list
async function getCategoryList(baseUrl, token) {
    try {
        const response = await axios.get(baseUrl + "/api/v1/category", {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error fetching category list:', error.response ? error.response.data : error.message);
        throw error;
    }
}

module.exports = {
    postData,
    getCategoryList
};
