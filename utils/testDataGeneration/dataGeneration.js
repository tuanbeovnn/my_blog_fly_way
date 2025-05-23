const moment = require('moment');
const {getListFeeds} = require("./axiosClient");

// Function to generate random category ID from an array
const getRandomId = (ids) => {
    const randomIndex = Math.floor(Math.random() * ids.length);
    return ids[randomIndex];
};

// Function to calculate the date with offset
const calculateDateWithOffset = (offset) => {
    if (offset.startsWith('{') && offset.endsWith(' days}')) {
        const daysOffset = parseInt(offset.substring(1, offset.length - 6));
        if (!isNaN(daysOffset)) {
            return moment().subtract(daysOffset, 'days').toISOString();
        }
    }
    return null;
};

// Function to handle fetching and posting data
const processPosts = async (url, token, getCategoryList, postData, postTemplate) => {
    try {
        const categories = await getCategoryList(url, token);
        const categoryIds = categories?.details?.records.map(category => category.id);
        const {posts} = postTemplate;

        for (const post of posts) {
            post.categoryId = getRandomId(categoryIds);

            if (post.createdDate) {
                const calculatedDate = calculateDateWithOffset(post.createdDate);
                if (calculatedDate) {
                    post.createdDate = calculatedDate;
                } else {
                    console.log('Invalid createdDate offset format:', post.createdDate);
                }
            }

            await postData(url, post, token);
        }
    } catch (error) {
        console.error('Error processing posts:', error);
    }
};

// Function to handle processing comments
const processComments = async (url, token, commentsData, commentTemplate) => {
    try {
        const {comments} = commentTemplate;
        const postLists = await getListFeeds(url, token);
        const categoryIds = postLists?.details?.records.map(post => post.id);
        for (const comment of comments) {
            comment.postId = getRandomId(categoryIds);
            await commentsData(url, comment, token);
        }
    } catch (error) {
        console.error('Error processing comments:', error);
    }
};

module.exports = {
    getRandomId,
    calculateDateWithOffset,
    processPosts,
    processComments
};
