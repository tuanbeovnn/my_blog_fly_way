const axios = require('axios');

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

async function getCategoryList(baseUrl, token) {
    try {
        const response = await axios.get(baseUrl + "/api/v1/public/category");
        return response.data;
    } catch (error) {
        console.error('Error fetching category list:', error.response ? error.response.data : error.message);
        throw error;
    }
}
async function commentsData(url, data, token) {
    try {
        const response = await axios.post(url + "/api/v1/comments", data, {
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

module.exports = {
    postData,
    commentsData,
    getCategoryList
};
